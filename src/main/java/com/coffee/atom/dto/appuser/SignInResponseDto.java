package com.coffee.atom.dto.appuser;

import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SignInResponseDto {
    private Long appUserId;
    private String userId;
    private String accessToken;
    private Role role;

    public SignInResponseDto(AppUser appUser, String accessToken) {
        this.appUserId = appUser.getId();
        this.userId = appUser.getUserId();
        this.role = appUser.getRole();
        this.accessToken = accessToken;
    }
}
