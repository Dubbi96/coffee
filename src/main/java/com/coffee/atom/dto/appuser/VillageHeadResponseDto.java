package com.coffee.atom.dto.appuser;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class VillageHeadResponseDto {
    private Long Id;
    private String appUserId;
    private String appUserName;
    private String sectionName;
    private Long farmerCount;
}
