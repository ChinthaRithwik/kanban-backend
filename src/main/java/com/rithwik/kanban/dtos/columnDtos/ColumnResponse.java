package com.rithwik.kanban.dtos.columnDtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ColumnResponse {

    private Long id;
    private String name;
    private Integer position;
}
