package com.rithwik.kanban.service;

import com.rithwik.kanban.entity.Board;
import com.rithwik.kanban.entity.BoardMember;
import com.rithwik.kanban.entity.Role;
import com.rithwik.kanban.entity.User;

import java.util.List;

public interface BoardMemberService {

    BoardMember addMember(Board board, User user, Role role);

    boolean isMember(Long boardId, Long userId);

    List<BoardMember> getMembers(Long boardId);
}
