package com.rithwik.kanban.repository;

import com.rithwik.kanban.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findByOwnerId(Long ownerId);

}