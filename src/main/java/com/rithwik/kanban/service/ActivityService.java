package com.rithwik.kanban.service;

import com.rithwik.kanban.entity.Activity;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ActivityService {

    Activity logActivity(Long boardId, String username, String message);

    List<Activity> getBoardActivities(Long boardId);

    Page<Activity> getRecentActivities(Long boardId, int page, int size);
}