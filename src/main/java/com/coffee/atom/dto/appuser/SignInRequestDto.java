package com.coffee.atom.dto.appuser;

import lombok.Data;

@Data
public class SignInRequestDto {
    private String userId;
    private String password;
}
