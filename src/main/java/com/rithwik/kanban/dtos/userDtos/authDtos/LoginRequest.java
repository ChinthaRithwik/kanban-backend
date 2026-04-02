package com.rithwik.kanban.dtos.userDtos.authDtos;

import lombok.Data;

@Data
public class LoginRequest {

    private String email;

    private String password;

}