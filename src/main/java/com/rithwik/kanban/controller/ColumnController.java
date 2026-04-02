package com.rithwik.kanban.controller;

import com.rithwik.kanban.dtos.columnDtos.ColumnResponse;
import com.rithwik.kanban.dtos.columnDtos.CreateColumnRequest;
import com.rithwik.kanban.dtos.columnDtos.UpdateColumnRequest;
import com.rithwik.kanban.service.ColumnService;
import com.rithwik.kanban.util.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/columns")
@RequiredArgsConstructor
public class ColumnController {

    private final ColumnService columnService;

    @GetMapping("/board/{boardId}")
    public ResponseEntity<ApiResponse<List<ColumnResponse>>> getColumnsByBoard(
            @PathVariable Long boardId) {

        List<ColumnResponse> columns = columnService.getColumnsByBoard(boardId);
        ApiResponse<List<ColumnResponse>> response =
                new ApiResponse<>(true, "Columns fetched successfully", columns);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ColumnResponse>> createColumn(
            @RequestBody CreateColumnRequest request) {

        String email = com.rithwik.kanban.util.CurrentUser.getEmail();
        ColumnResponse column = columnService.createColumn(request, email);
        ApiResponse<ColumnResponse> response =
                new ApiResponse<>(true, "Column created successfully", column);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{columnId}")
    public ResponseEntity<ApiResponse<ColumnResponse>> updateColumn(
            @PathVariable Long columnId,
            @RequestBody UpdateColumnRequest request) {

        String email = com.rithwik.kanban.util.CurrentUser.getEmail();
        ColumnResponse column = columnService.updateColumn(columnId, request, email);
        ApiResponse<ColumnResponse> response =
                new ApiResponse<>(true, "Column updated successfully", column);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{columnId}")
    public ResponseEntity<ApiResponse<Void>> deleteColumn(
            @PathVariable Long columnId) {

        String email = com.rithwik.kanban.util.CurrentUser.getEmail();
        columnService.deleteColumn(columnId, email);
        ApiResponse<Void> response =
                new ApiResponse<>(true, "Column deleted successfully", null);
        return ResponseEntity.ok(response);
    }
}
