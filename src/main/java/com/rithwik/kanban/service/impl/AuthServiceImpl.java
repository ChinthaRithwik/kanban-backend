package com.rithwik.kanban.service.impl;

import com.rithwik.kanban.dtos.userDtos.authDtos.AuthResponse;
import com.rithwik.kanban.dtos.userDtos.authDtos.LoginRequest;
import com.rithwik.kanban.dtos.userDtos.authDtos.SignupRequest;
import com.rithwik.kanban.entity.User;
import com.rithwik.kanban.repository.UserRepository;
import com.rithwik.kanban.security.JwtUtil;
import com.rithwik.kanban.exception.UnauthorizedException;
import com.rithwik.kanban.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Authentication service.
 *
 * Both register and login call jwtUtil.generateToken(id, email) with the
 * database-assigned user ID, guaranteeing the JWT "id" claim is always the
 * real numeric ID (never null, never a temp value).
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public AuthResponse register(SignupRequest request) {
        // Reject duplicate emails early with a clear message
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // ID is null until save(); token is generated AFTER save so the ID is real
        User saved = userRepository.save(user);

        String token = jwtUtil.generateToken(saved.getId(), saved.getEmail());
        return new AuthResponse(token);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token);
    }
}
