package com.rithwik.kanban.dtos.taskDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoveTaskRequest {

    private Long taskId;
    private Long sourceColumnId;
    private Long destinationColumnId;
    private Integer newPosition;

}
