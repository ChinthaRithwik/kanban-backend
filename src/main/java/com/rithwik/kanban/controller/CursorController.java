
package com.rithwik.kanban.controller;

import com.rithwik.kanban.dtos.presenceDtos.CursorEvent;
import com.rithwik.kanban.entity.User;
import com.rithwik.kanban.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class CursorController {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;
    @MessageMapping("/board/{boardId}/cursor")
    public void sendCursor(
            @DestinationVariable Long boardId,
            CursorEvent incoming,
            Principal principal
    ) {

        if (principal == null) return;

        String email = principal.getName();
        System.out.println("WS USER: " + email);

        String displayName = userRepository.findByEmail(email)
                .map(User::getName)   // or getFullName()
                .orElse(email); // fallback

        CursorEvent event = new CursorEvent(
                displayName,
                boardId,
                incoming.getX(),
                incoming.getY()
        );

        messagingTemplate.convertAndSend(
                "/topic/board/" + boardId + "/cursor",
                event
        );
    }
}