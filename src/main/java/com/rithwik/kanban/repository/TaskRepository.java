package com.rithwik.kanban.repository;

import com.rithwik.kanban.entity.Task;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByColumnIdOrderByPosition(Long columnId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Task t WHERE t.column.id = :columnId ORDER BY t.position ASC")
    List<Task> findByColumnIdOrderByPositionForUpdate(@Param("columnId") Long columnId);

}