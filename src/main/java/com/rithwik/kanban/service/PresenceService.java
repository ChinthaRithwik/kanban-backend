package com.rithwik.kanban.service;

public interface PresenceService {

    void addUser(Long boardId, String username);

    void removeUser(Long boardId, String username);

}