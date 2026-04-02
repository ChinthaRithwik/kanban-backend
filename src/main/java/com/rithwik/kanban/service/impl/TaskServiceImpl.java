package com.rithwik.kanban.service.impl;

import com.rithwik.kanban.dtos.taskDtos.*;
import com.rithwik.kanban.dtos.websocket.ActivityMessage;
import com.rithwik.kanban.dtos.websocket.KanbanEvent;
import com.rithwik.kanban.entity.BoardColumn;
import com.rithwik.kanban.entity.Task;
import com.rithwik.kanban.entity.User;
import com.rithwik.kanban.exception.ResourceNotFoundException;
import com.rithwik.kanban.exception.UnauthorizedException;
import com.rithwik.kanban.repository.BoardMemberRepository;
import com.rithwik.kanban.repository.ColumnRepository;
import com.rithwik.kanban.repository.TaskRepository;
import com.rithwik.kanban.repository.UserRepository;
import com.rithwik.kanban.service.ActivityService;
import com.rithwik.kanban.service.TaskService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final ColumnRepository columnRepository;
    private final UserRepository userRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ModelMapper modelMapper;
    private final ActivityService activityService;

    // ✅ SINGLE SOURCE OF TRUTH FOR MESSAGE
    private String buildMessage(KanbanEvent event, String name) {
        return switch (event.getType()) {
            case "TASK_CREATED" ->
                    name + " created task '" + event.getTaskTitle() + "'";
            case "TASK_MOVED" ->
                    name + " moved task '" + event.getTaskTitle() +
                            "' to " + event.getDestinationColumnName();
            case "TASK_UPDATED" ->
                    name + " updated task '" + event.getTaskTitle() + "'";
            case "TASK_DELETED" ->
                    name + " deleted task '" + event.getTaskTitle() + "'";
            default -> "";
        };
    }

    // ✅ BROADCAST + SAVE (NO DUPLICATION)
    private void broadcast(Long boardId, KanbanEvent event, String email) {
        String name = userRepository.findByEmail(email)
                .map(User::getName)
                .orElse(email);
        System.out.println("SERVICE USER: " + name);

        event.setPerformedBy(name);
        String message = buildMessage(event, name);

        // 1. Persist first — gives us the DB id and authoritative createdAt.
        com.rithwik.kanban.entity.Activity activity = activityService.logActivity(
                boardId,
                name,
                message
        );

        // 2. Board-level event (task/column state change)
        messagingTemplate.convertAndSend(
                "/topic/board/" + boardId,
                event
        );

        // 3. Activity WebSocket
        messagingTemplate.convertAndSend(
                "/topic/board/" + boardId + "/activity",
                new ActivityMessage(
                        event.getType(),
                        name,
                        message,
                        activity.getId(),
                        activity.getCreatedAt()
                )
        );
    }

    private void assertBoardMember(Long boardId, String email) {
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
        if (!boardMemberRepository.findByBoardIdAndUserId(boardId, userId).isPresent()) {
            throw new UnauthorizedException("You are not a member of this board");
        }
    }

    // =============================

    @Override
    public Task createTask(Long columnId, String title, String description) {
        BoardColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Column not found"));

        List<Task> existing = taskRepository.findByColumnIdOrderByPosition(columnId);

        Task task = Task.builder()
                .title(title)
                .description(description)
                .position(existing.size())
                .createdAt(LocalDateTime.now())
                .column(column)
                .build();

        return taskRepository.save(task);
    }

    @Override
    public List<Task> getColumnTasks(Long columnId) {
        return taskRepository.findByColumnIdOrderByPosition(columnId);
    }

    @Override
    public TaskResponse createTask(CreateTaskRequest request, String email) {
        BoardColumn column = columnRepository.findById(request.getColumnId())
                .orElseThrow(() -> new ResourceNotFoundException("Column not found"));
        assertBoardMember(column.getBoard().getId(), email);

        User user = null;
        if (request.getAssignedUserId() != null) {
            user = userRepository.findById(request.getAssignedUserId()).orElse(null);
        }

        List<Task> tasks = taskRepository.findByColumnIdOrderByPosition(request.getColumnId());

        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setColumn(column);
        task.setAssignedUser(user);
        task.setPosition(tasks.size());
        task.setCreatedAt(LocalDateTime.now());

        Task saved = taskRepository.save(task);

        KanbanEvent event = new KanbanEvent();
        event.setType("TASK_CREATED");
        event.setTaskId(saved.getId());
        event.setTaskTitle(saved.getTitle());
        event.setDescription(saved.getDescription());
        event.setDestinationColumnId(column.getId());
        event.setDestinationColumnName(column.getName());
        event.setNewPosition(saved.getPosition());
        broadcast(column.getBoard().getId(), event, email);

        return modelMapper.map(saved, TaskResponse.class);
    }

    @Override
    public List<TaskResponse> getTasksByColumn(Long columnId) {
        columnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Column not found"));

        return taskRepository.findByColumnIdOrderByPosition(columnId).stream()
                .map(t -> {
                    TaskResponse r = new TaskResponse();
                    r.setId(t.getId());
                    r.setTitle(t.getTitle());
                    r.setDescription(t.getDescription());
                    r.setPosition(t.getPosition());
                    return r;
                }).toList();
    }

    @Override
    @Transactional
    public void moveTask(MoveTaskRequest request, String email) {

        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        BoardColumn source = columnRepository.findById(request.getSourceColumnId())
                .orElseThrow(() -> new ResourceNotFoundException("Source column not found"));
        assertBoardMember(source.getBoard().getId(), email);

        BoardColumn destination = columnRepository.findById(request.getDestinationColumnId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination column not found"));

        boolean isSameColumn = source.getId().equals(destination.getId());

        int pos = request.getNewPosition() != null ? request.getNewPosition() : 0;
        if (pos < 0) pos = 0;

        if (isSameColumn) {
            List<Task> tasks = taskRepository.findByColumnIdOrderByPositionForUpdate(source.getId());

            tasks.removeIf(t -> t.getId().equals(task.getId()));
            pos = Math.min(pos, tasks.size());
            tasks.add(pos, task);

            for (int i = 0; i < tasks.size(); i++) {
                tasks.get(i).setPosition(i);
            }

            taskRepository.saveAll(tasks);

        } else {
            List<Task> sourceTasks = taskRepository.findByColumnIdOrderByPositionForUpdate(source.getId());
            List<Task> destTasks = taskRepository.findByColumnIdOrderByPositionForUpdate(destination.getId());

            sourceTasks.removeIf(t -> t.getId().equals(task.getId()));
            for (int i = 0; i < sourceTasks.size(); i++) {
                sourceTasks.get(i).setPosition(i);
            }

            task.setColumn(destination);
            pos = Math.min(pos, destTasks.size());
            destTasks.add(pos, task);

            for (int i = 0; i < destTasks.size(); i++) {
                destTasks.get(i).setPosition(i);
            }

            taskRepository.saveAll(sourceTasks);
            taskRepository.saveAll(destTasks);
        }

        KanbanEvent event = new KanbanEvent();
        event.setType("TASK_MOVED");
        event.setTaskId(task.getId());
        event.setTaskTitle(task.getTitle());
        event.setSourceColumnId(source.getId());
        event.setSourceColumnName(source.getName());
        event.setDestinationColumnId(destination.getId());
        event.setDestinationColumnName(destination.getName());
        event.setNewPosition(pos);


        broadcast(task.getColumn().getBoard().getId(), event, email);
    }

    @Override
    public TaskResponse updateTask(Long taskId, UpdateTaskRequest request, String email) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));
        assertBoardMember(task.getColumn().getBoard().getId(), email);

        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());

        if (request.getAssignedUserId() != null) {
            userRepository.findById(request.getAssignedUserId())
                    .ifPresent(task::setAssignedUser);
        }

        taskRepository.save(task);

        KanbanEvent event = new KanbanEvent();
        event.setType("TASK_UPDATED");
        event.setTaskId(task.getId());
        event.setTaskTitle(task.getTitle());
        event.setDescription(task.getDescription());
        event.setSourceColumnId(task.getColumn().getId());
        event.setDestinationColumnId(task.getColumn().getId());
        event.setNewPosition(task.getPosition());


        broadcast(task.getColumn().getBoard().getId(), event, email);
        return modelMapper.map(task, TaskResponse.class);
    }

    @Override
    public void deleteTask(Long taskId, String email) {

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        BoardColumn column = task.getColumn();
        Long boardId = column.getBoard().getId();
        assertBoardMember(boardId, email);
        Long colId = column.getId();
        String title = task.getTitle();

        List<Task> tasks = taskRepository.findByColumnIdOrderByPosition(colId);

        tasks.removeIf(t -> t.getId().equals(task.getId()));

        for (int i = 0; i < tasks.size(); i++) {
            tasks.get(i).setPosition(i);
        }

        taskRepository.delete(task);
        taskRepository.saveAll(tasks);

        KanbanEvent event = new KanbanEvent();
        event.setType("TASK_DELETED");
        event.setTaskId(taskId);
        event.setTaskTitle(title);
        event.setSourceColumnId(colId);
        event.setDestinationColumnId(colId);
        event.setNewPosition(-1);
        broadcast(boardId, event, email);
    }
}