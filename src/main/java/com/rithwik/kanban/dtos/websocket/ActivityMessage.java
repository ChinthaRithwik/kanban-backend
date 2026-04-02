package com.rithwik.kanban.dtos.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import com.fasterxml.jackson.annotation.JsonFormat;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityMessage {
    private String type;
    private String user;
    private String message;
    private Long id;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    public ActivityMessage(String type, String user, String message) {
        this.type = type;
        this.user = user;
        this.message = message;
    }
}