package com.rithwik.kanban.dtos.taskDtos;

import lombok.Data;

@Data
public class UpdateTaskRequest {

    private String title;

    private String description;

    private Long assignedUserId;

}