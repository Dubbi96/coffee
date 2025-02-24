package com.coffee.atom.dto.appuser;

import lombok.Data;

@Data
public class AppUserStatusUpdateRequestDto {
    private String userId;
    private String username;
}
