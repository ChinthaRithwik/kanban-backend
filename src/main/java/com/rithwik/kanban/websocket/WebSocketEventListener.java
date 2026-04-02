package com.rithwik.kanban.websocket;


import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import com.rithwik.kanban.service.PresenceService;

@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;

    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {

        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        if (accessor.getUser() == null) return;

        String username = accessor.getUser().getName();

        Long boardId = (Long) accessor.getSessionAttributes().get("boardId");

        if (boardId != null) {
            presenceService.removeUser(boardId, username);
        }
    }
}