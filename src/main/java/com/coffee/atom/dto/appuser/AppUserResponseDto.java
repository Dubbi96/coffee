package com.coffee.atom.dto.appuser;

import com.coffee.atom.domain.appuser.AppUser;
import lombok.Data;

@Data
public class AppUserResponseDto {
    private Long appUserId;
    private String userId;
    private String username;

    public AppUserResponseDto(AppUser user) {
        this.appUserId = user.getId();
        this.userId = user.getUserId();
        this.username = user.getUsername();
    }
}