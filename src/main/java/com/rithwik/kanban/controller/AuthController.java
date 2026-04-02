package com.rithwik.kanban.controller;

import com.rithwik.kanban.dtos.userDtos.authDtos.LoginRequest;
import com.rithwik.kanban.dtos.userDtos.authDtos.AuthResponse;
import com.rithwik.kanban.dtos.userDtos.authDtos.SignupRequest;
import com.rithwik.kanban.service.AuthService;
import com.rithwik.kanban.util.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthResponse>> signup(
            @RequestBody SignupRequest request) {

        AuthResponse response = authService.register(request);

        ApiResponse<AuthResponse> apiResponse =
                new ApiResponse<>(true, "User registered successfully", response);

        return ResponseEntity.ok(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        ApiResponse<AuthResponse> apiResponse =
                new ApiResponse<>(true, "Login successful", response);

        return ResponseEntity.ok(apiResponse);
    }
}