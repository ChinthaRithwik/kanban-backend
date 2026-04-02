package com.rithwik.kanban.controller;

import com.rithwik.kanban.dtos.taskDtos.CreateTaskRequest;
import com.rithwik.kanban.dtos.taskDtos.MoveTaskRequest;
import com.rithwik.kanban.dtos.taskDtos.TaskResponse;
import com.rithwik.kanban.dtos.taskDtos.UpdateTaskRequest;
import com.rithwik.kanban.service.TaskService;
import com.rithwik.kanban.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @RequestBody CreateTaskRequest request) {

        String email = com.rithwik.kanban.util.CurrentUser.getEmail();
        TaskResponse task = taskService.createTask(request, email);

        ApiResponse<TaskResponse> response =
                new ApiResponse<>(true, "Task created successfully", task);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/column/{columnId}")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> getTasksByColumn(
            @PathVariable Long columnId) {

        List<TaskResponse> tasks = taskService.getTasksByColumn(columnId);

        ApiResponse<List<TaskResponse>> response =
                new ApiResponse<>(true, "Tasks fetched successfully", tasks);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/move")
    public ResponseEntity<ApiResponse<Void>> moveTask(
            @RequestBody MoveTaskRequest request) {

        String email = com.rithwik.kanban.util.CurrentUser.getEmail();
        taskService.moveTask(request, email);

        ApiResponse<Void> response =
                new ApiResponse<>(true, "Task moved successfully", null);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long taskId,
            @RequestBody UpdateTaskRequest request) {

        String email = com.rithwik.kanban.util.CurrentUser.getEmail();
        TaskResponse updatedTask = taskService.updateTask(taskId, request, email);

        ApiResponse<TaskResponse> response =
                new ApiResponse<>(true, "Task updated successfully", updatedTask);

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(
            @PathVariable Long taskId) {

        String email = com.rithwik.kanban.util.CurrentUser.getEmail();
        taskService.deleteTask(taskId, email);

        ApiResponse<Void> response =
                new ApiResponse<>(true, "Task deleted successfully", null);

        return ResponseEntity.ok(response);
    }
}