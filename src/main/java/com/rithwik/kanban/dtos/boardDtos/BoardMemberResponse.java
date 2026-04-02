package com.rithwik.kanban.dtos.boardDtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BoardMemberResponse {

    @JsonProperty("user_id")
    private Long userId;

    private String name;
    private String email;
    private String role;
}
