package com.rithwik.kanban.service.impl;

import com.rithwik.kanban.entity.Board;
import com.rithwik.kanban.entity.BoardMember;
import com.rithwik.kanban.entity.Role;
import com.rithwik.kanban.entity.User;
import com.rithwik.kanban.exception.ResourceNotFoundException;
import com.rithwik.kanban.repository.BoardMemberRepository;
import com.rithwik.kanban.service.BoardMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardMemberServiceImpl implements BoardMemberService {

    private final BoardMemberRepository boardMemberRepository;

    @Override
    public BoardMember addMember(Board board, User user, Role role) {
        boardMemberRepository.findByBoardIdAndUserId(board.getId(), user.getId())
                .ifPresent(m -> {
                    throw new IllegalStateException(
                            "User " + user.getId() + " is already a member of board " + board.getId());
                });

        BoardMember member = BoardMember.builder()
                .board(board)
                .user(user)
                .role(role)
                .joinedAt(LocalDateTime.now())
                .build();

        return boardMemberRepository.save(member);
    }

    @Override
    public boolean isMember(Long boardId, Long userId) {
        return boardMemberRepository.findByBoardIdAndUserId(boardId, userId).isPresent();
    }

    @Override
    public List<BoardMember> getMembers(Long boardId) {
        return boardMemberRepository.findByBoardId(boardId);
    }
}
