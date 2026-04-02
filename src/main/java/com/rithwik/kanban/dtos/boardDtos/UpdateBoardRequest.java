package com.rithwik.kanban.dtos.boardDtos;

import lombok.Data;

@Data
public class UpdateBoardRequest {
    private String name;
    private String description;
}
