package com.rithwik.kanban.controller;

import com.rithwik.kanban.entity.Activity;
import com.rithwik.kanban.exception.ResourceNotFoundException;
import com.rithwik.kanban.exception.UnauthorizedException;
import com.rithwik.kanban.repository.BoardMemberRepository;
import com.rithwik.kanban.repository.UserRepository;
import com.rithwik.kanban.service.ActivityService;
import com.rithwik.kanban.util.ApiResponse;
import com.rithwik.kanban.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final BoardMemberRepository boardMemberRepository;
    private final UserRepository userRepository;

    /**
     * GET /api/boards/{boardId}/activities?page=0&size=50
     * Returns paginated activity log, newest first.
     */
    @GetMapping("/{boardId}/activities")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getActivities(
            @PathVariable Long boardId,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "50") int size) {

        String email = CurrentUser.getEmail();
        Long userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"))
                .getId();
        if (!boardMemberRepository.findByBoardIdAndUserId(boardId, userId).isPresent()) {
            throw new UnauthorizedException("Not a board member");
        }

        Page<Activity> result = activityService.getRecentActivities(boardId, page, size);

        Map<String, Object> payload = Map.of(
                "activities",  result.getContent(),
                "totalPages",  result.getTotalPages(),
                "currentPage", result.getNumber(),
                "hasMore",     !result.isLast()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Activities fetched", payload));
    }
}
