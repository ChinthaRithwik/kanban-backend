package com.rithwik.kanban.dtos.userDtos.authDtos;

import lombok.Data;

@Data
public class SignupRequest {

    private String name;

    private String email;

    private String password;

}