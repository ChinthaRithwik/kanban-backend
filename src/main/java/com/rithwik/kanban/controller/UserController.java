package com.rithwik.kanban.controller;

import com.rithwik.kanban.entity.User;
import com.rithwik.kanban.repository.UserRepository;
import com.rithwik.kanban.util.ApiResponse;
import com.rithwik.kanban.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCurrentUser() {
        String email = CurrentUser.getEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("name", user.getName());
        userData.put("email", user.getEmail());

        ApiResponse<Map<String, Object>> response =
                new ApiResponse<>(true, "User fetched successfully", userData);

        return ResponseEntity.ok(response);
    }
}
