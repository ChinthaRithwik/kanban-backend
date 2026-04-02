package com.rithwik.kanban.dtos.taskDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTaskRequest {

    private String title;
    private String description;
    private Long columnId;
    private Long assignedUserId;
}
