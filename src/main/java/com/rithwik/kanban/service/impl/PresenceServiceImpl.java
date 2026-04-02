package com.rithwik.kanban.service.impl;

import com.rithwik.kanban.dtos.presenceDtos.UserPresenceDTO;
import com.rithwik.kanban.service.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class PresenceServiceImpl implements PresenceService {

    private final SimpMessagingTemplate messagingTemplate;

    private final Map<Long, Map<String, Integer>> userConnectionCountMap = new ConcurrentHashMap<>();
    private final Map<Long, Set<UserPresenceDTO>> boardPresenceMap = new ConcurrentHashMap<>();

    @Override
    public void addUser(Long boardId, String username) {

        userConnectionCountMap.putIfAbsent(boardId, new ConcurrentHashMap<>());
        Map<String, Integer> userCountMap = userConnectionCountMap.get(boardId);

        userCountMap.put(username, userCountMap.getOrDefault(username, 0) + 1);

        if (userCountMap.get(username) == 1) {
            boardPresenceMap
                .computeIfAbsent(boardId, k -> ConcurrentHashMap.newKeySet())
                .add(new UserPresenceDTO(username));
        }

        broadcast(boardId);
    }

    @Override
    public void removeUser(Long boardId, String username) {

        Map<String, Integer> userCountMap = userConnectionCountMap.get(boardId);

        if (userCountMap == null) return;

        int count = userCountMap.getOrDefault(username, 0);

        if (count <= 1) {
            userCountMap.remove(username);

            Set<UserPresenceDTO> users = boardPresenceMap.get(boardId);
            if (users != null) {
                users.removeIf(u -> u.getUsername().equals(username));
            }

        } else {
            userCountMap.put(username, count - 1);
        }

        broadcast(boardId);
    }

    private void broadcast(Long boardId) {
        messagingTemplate.convertAndSend(
            "/topic/board/" + boardId + "/presence",
            boardPresenceMap.getOrDefault(boardId, Set.of())
        );
    }
}