package com.rithwik.kanban.dtos.presenceDtos;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CursorEvent {

    private String username;
    private Long boardId;
    private int x;
    private int y;
}