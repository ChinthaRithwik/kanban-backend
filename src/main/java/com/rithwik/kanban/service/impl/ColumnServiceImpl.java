package com.rithwik.kanban.service.impl;

import com.rithwik.kanban.dtos.columnDtos.ColumnResponse;
import com.rithwik.kanban.dtos.columnDtos.CreateColumnRequest;
import com.rithwik.kanban.dtos.columnDtos.UpdateColumnRequest;
import com.rithwik.kanban.dtos.websocket.ActivityMessage;
import com.rithwik.kanban.dtos.websocket.KanbanEvent;
import com.rithwik.kanban.entity.Board;
import com.rithwik.kanban.entity.BoardColumn;
import com.rithwik.kanban.entity.User;
import com.rithwik.kanban.exception.ResourceNotFoundException;
import com.rithwik.kanban.exception.UnauthorizedException;
import com.rithwik.kanban.repository.BoardMemberRepository;
import com.rithwik.kanban.repository.BoardRepository;
import com.rithwik.kanban.repository.ColumnRepository;
import com.rithwik.kanban.repository.UserRepository;
import com.rithwik.kanban.service.ActivityService;
import com.rithwik.kanban.service.ColumnService;
import com.rithwik.kanban.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ColumnServiceImpl implements ColumnService {

    private final ColumnRepository columnRepository;
    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final ModelMapper modelMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    // ✅ SINGLE MESSAGE BUILDER
    private String buildMessage(KanbanEvent event, String name) {
        return switch (event.getType()) {
            case "COLUMN_CREATED" ->
                    name + " created column '" + event.getColumnName() + "'";
            case "COLUMN_UPDATED" ->
                    name + " renamed column to '" + event.getColumnName() + "'";
            case "COLUMN_DELETED" ->
                    name + " deleted column '" + event.getColumnName() + "'";
            default -> "";
        };
    }

    // ✅ BROADCAST + SAVE
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
    public BoardColumn createColumn(Long boardId, String columnName) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        List<BoardColumn> existingColumns =
                columnRepository.findByBoardIdOrderByPosition(boardId);

        BoardColumn column = BoardColumn.builder()
                .name(columnName)
                .position(existingColumns.size())
                .board(board)
                .build();

        return columnRepository.save(column);
    }

    @Override
    public List<BoardColumn> getBoardColumns(Long boardId) {
        return columnRepository.findByBoardIdOrderByPosition(boardId);
    }

    @Override
    public List<ColumnResponse> getColumnsByBoard(Long boardId) {
        boardRepository.findById(boardId)
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));

        return columnRepository.findByBoardIdOrderByPosition(boardId)
                .stream()
                .map(col -> {
                    ColumnResponse r = new ColumnResponse();
                    r.setId(col.getId());
                    r.setName(col.getName());
                    r.setPosition(col.getPosition());
                    return r;
                })
                .collect(Collectors.toList());
    }

    @Override
    public ColumnResponse createColumn(CreateColumnRequest request, String email) {

        Board board = boardRepository.findById(request.getBoardId())
                .orElseThrow(() -> new ResourceNotFoundException("Board not found"));
        assertBoardMember(board.getId(), email);

        List<BoardColumn> columns =
                columnRepository.findByBoardIdOrderByPosition(board.getId());

        BoardColumn column = new BoardColumn();
        column.setName(request.getName());
        column.setBoard(board);
        column.setPosition(columns.size());

        columnRepository.save(column);

        KanbanEvent event = new KanbanEvent();
        event.setType("COLUMN_CREATED");
        event.setColumnId(column.getId());
        event.setColumnName(column.getName());
        broadcast(board.getId(), event, email);

        return modelMapper.map(column, ColumnResponse.class);
    }

    @Override
    public ColumnResponse updateColumn(Long columnId, UpdateColumnRequest request, String email) {

        BoardColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Column not found"));
        assertBoardMember(column.getBoard().getId(), email);

        if (request.getName() != null && !request.getName().isBlank()) {
            column.setName(request.getName());
        }

        columnRepository.save(column);

        KanbanEvent event = new KanbanEvent();
        event.setType("COLUMN_UPDATED");
        event.setColumnId(column.getId());
        event.setColumnName(column.getName());
        broadcast(column.getBoard().getId(), event, email);

        ColumnResponse response = new ColumnResponse();
        response.setId(column.getId());
        response.setName(column.getName());
        response.setPosition(column.getPosition());

        return response;
    }

    @Override
    public void deleteColumn(Long columnId, String email) {

        BoardColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new ResourceNotFoundException("Column not found"));

        Long boardId = column.getBoard().getId();
        assertBoardMember(boardId, email);
        String name = column.getName();

        columnRepository.delete(column);

        KanbanEvent event = new KanbanEvent();
        event.setType("COLUMN_DELETED");
        event.setColumnId(columnId);
        event.setColumnName(name);
        broadcast(boardId, event, email);
    }
}