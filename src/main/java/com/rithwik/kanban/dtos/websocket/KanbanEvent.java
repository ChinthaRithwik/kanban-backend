package com.rithwik.kanban.dtos.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KanbanEvent {

    private String type;

    // Task events
    private Long taskId;
    private String taskTitle;
    private String description;
    private Long sourceColumnId;
    private String sourceColumnName;
    private Long destinationColumnId;
    private String destinationColumnName;
    private Integer newPosition;

    // Column events
    private Long columnId;
    private String columnName;

    // Who performed the action
    private String performedBy;

    // Convenience constructor for task move events (backward compat)
    public KanbanEvent(String type, Long taskId, Long sourceColumnId,
                       Long destinationColumnId, Integer newPosition) {
        this.type = type;
        this.taskId = taskId;
        this.sourceColumnId = sourceColumnId;
        this.destinationColumnId = destinationColumnId;
        this.newPosition = newPosition;
    }
}
