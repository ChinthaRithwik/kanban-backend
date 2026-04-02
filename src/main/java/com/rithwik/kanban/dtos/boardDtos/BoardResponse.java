package com.rithwik.kanban.dtos.boardDtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BoardResponse {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private Long ownerId;

}