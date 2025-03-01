package com.coffee.atom.dto.appuser;

import lombok.Data;

@Data
public class SignUpRequestDto {
    private String userId;
    private String username;
    private String password;
    private String role;
}