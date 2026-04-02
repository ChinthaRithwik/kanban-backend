package com.rithwik.kanban.service;

import com.rithwik.kanban.dtos.taskDtos.CreateTaskRequest;
import com.rithwik.kanban.dtos.taskDtos.MoveTaskRequest;
import com.rithwik.kanban.dtos.taskDtos.TaskResponse;
import com.rithwik.kanban.dtos.taskDtos.UpdateTaskRequest;
import com.rithwik.kanban.entity.Task;

import java.util.List;

public interface TaskService {

    Task createTask(Long columnId, String title, String description);

    List<Task> getColumnTasks(Long columnId);

    TaskResponse createTask(CreateTaskRequest request, String email);

    List<TaskResponse> getTasksByColumn(Long columnId);

    void moveTask(MoveTaskRequest request, String email);

    TaskResponse updateTask(Long taskId, UpdateTaskRequest request, String email);

    void deleteTask(Long taskId, String email);
}
