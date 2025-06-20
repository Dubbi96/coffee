package com.coffee.atom.dto.appuser;

import com.coffee.atom.dto.area.AreaDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ViceAdminMyInfoDto {
    private AppUserInfoDto appUser;
    private String idCardUrl;
    private AreaDto area;
}