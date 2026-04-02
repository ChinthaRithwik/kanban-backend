package com.rithwik.kanban.dtos.allDto;

import lombok.Data;
import java.util.List;

@Data
public class BoardFullResponse {

    private Long id;
    private String name;

    private List<ColumnWithTasksResponse> columns;

}