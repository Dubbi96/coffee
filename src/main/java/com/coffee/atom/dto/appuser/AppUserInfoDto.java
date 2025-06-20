package com.coffee.atom.dto.appuser;

import com.coffee.atom.domain.appuser.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AppUserInfoDto {
    private Long id;
    private String userId;
    private String username;
    private Role role;
}