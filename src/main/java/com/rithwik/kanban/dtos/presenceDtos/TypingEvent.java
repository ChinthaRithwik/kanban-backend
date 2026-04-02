package com.rithwik.kanban.dtos.presenceDtos;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TypingEvent {

    private String username;
    private Long boardId;
}