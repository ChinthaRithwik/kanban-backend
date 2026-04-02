package com.rithwik.kanban.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String description;

    private String priority;

    private Integer position;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "column_id")
    private BoardColumn column;

    @ManyToOne
    @JoinColumn(name = "assigned_user_id")
    private User assignedUser;
}