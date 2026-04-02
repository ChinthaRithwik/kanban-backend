package com.rithwik.kanban.controller;

import com.rithwik.kanban.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    @MessageMapping("/board/{boardId}/join")
    public void joinBoard(@DestinationVariable Long boardId,
                          Principal principal,
                          SimpMessageHeaderAccessor headerAccessor) {

        if (principal == null) {
            throw new RuntimeException("User not authenticated in WebSocket");
        }

        headerAccessor.getSessionAttributes().put("boardId", boardId);

        System.out.println("WS USER: " + principal.getName());
        presenceService.addUser(boardId, principal.getName());
    }

}