package com.rithwik.kanban.repository;

import com.rithwik.kanban.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    List<Activity> findByBoardIdOrderByCreatedAtDesc(Long boardId);

    Page<Activity> findByBoardIdOrderByCreatedAtDesc(Long boardId, Pageable pageable);
}