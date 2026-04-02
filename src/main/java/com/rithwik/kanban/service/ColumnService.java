package com.rithwik.kanban.service;

import com.rithwik.kanban.dtos.columnDtos.ColumnResponse;
import com.rithwik.kanban.dtos.columnDtos.CreateColumnRequest;
import com.rithwik.kanban.dtos.columnDtos.UpdateColumnRequest;
import com.rithwik.kanban.entity.BoardColumn;

import java.util.List;

public interface ColumnService {

    BoardColumn createColumn(Long boardId, String columnName);

    List<BoardColumn> getBoardColumns(Long boardId);

    List<ColumnResponse> getColumnsByBoard(Long boardId);

    ColumnResponse createColumn(CreateColumnRequest request, String email);

    ColumnResponse updateColumn(Long columnId, UpdateColumnRequest request, String email);

    void deleteColumn(Long columnId, String email);
}
