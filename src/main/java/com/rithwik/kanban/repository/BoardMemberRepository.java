package com.rithwik.kanban.repository;

import com.rithwik.kanban.entity.BoardMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BoardMemberRepository extends JpaRepository<BoardMember, Long> {

    Optional<BoardMember> findByBoardIdAndUserId(Long boardId, Long userId);

    List<BoardMember> findByBoardId(Long boardId);

    List<BoardMember> findByUserId(Long userId);
}
