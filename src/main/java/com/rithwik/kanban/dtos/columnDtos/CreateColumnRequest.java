package com.rithwik.kanban.dtos.columnDtos;

import lombok.Data;

@Data
public class CreateColumnRequest {

    private Long boardId;
    private String name;

}