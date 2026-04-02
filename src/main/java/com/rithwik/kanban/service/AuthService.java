package com.rithwik.kanban.service;

import com.rithwik.kanban.dtos.userDtos.authDtos.LoginRequest;
import com.rithwik.kanban.dtos.userDtos.authDtos.SignupRequest;
import com.rithwik.kanban.dtos.userDtos.authDtos.AuthResponse;

public interface AuthService {

    AuthResponse register(SignupRequest request);

    AuthResponse login(LoginRequest request);

}