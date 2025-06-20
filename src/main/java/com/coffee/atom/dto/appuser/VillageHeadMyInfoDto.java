package com.coffee.atom.dto.appuser;

import com.coffee.atom.dto.area.AreaDto;
import com.coffee.atom.dto.area.SectionDto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VillageHeadMyInfoDto {
    private AppUserInfoDto appUser;
    private String identificationPhotoUrl;
    private String bankName;
    private String accountInfo;
    private String contractUrl;
    private String bankbookUrl;
    private SectionDto section;
    private AreaDto area;
}