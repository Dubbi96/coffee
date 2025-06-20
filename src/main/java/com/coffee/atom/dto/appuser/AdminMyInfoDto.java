package com.coffee.atom.dto.appuser;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminMyInfoDto {
    private AppUserInfoDto appUser;
}
