package com.rithwik.kanban.service.impl;

import com.rithwik.kanban.entity.Activity;
import com.rithwik.kanban.repository.ActivityRepository;
import com.rithwik.kanban.service.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

    private final ActivityRepository activityRepository;

    @Override
    public Activity logActivity(Long boardId, String username, String message) {

        Activity activity = Activity.builder()
                .boardId(boardId)
                .username(username)
                .message(message)
                .createdAt(Instant.now())
                .build();

        return activityRepository.save(activity);
    }

    @Override
    public List<Activity> getBoardActivities(Long boardId) {
        return activityRepository.findByBoardIdOrderByCreatedAtDesc(boardId);
    }

    @Override
    public Page<Activity> getRecentActivities(Long boardId, int page, int size) {
        return activityRepository.findByBoardIdOrderByCreatedAtDesc(
                boardId, PageRequest.of(page, size));
    }
}