package com.rithwik.kanban.dtos.allDto;


import com.rithwik.kanban.dtos.taskDtos.TaskResponse;
import lombok.Data;
import java.util.List;

@Data
public class ColumnWithTasksResponse {

    private Long id;

    private String name;

    private Integer position;

    private List<TaskResponse> tasks;

}