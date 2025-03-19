package com.coffee.atom.dto.appuser;

import com.coffee.atom.domain.appuser.Role;
import lombok.Data;

@Data
public class SignUpRequestDto {
    private String userId;
    private String username;
    private String password;
    private Role role;
}