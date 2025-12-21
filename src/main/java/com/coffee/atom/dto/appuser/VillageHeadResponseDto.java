package com.coffee.atom.dto.appuser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VillageHeadResponseDto {
    private Long id;
    private String appUserId;
    private String appUserName;
    private String sectionName;
    private Long farmerCount;
    private AreaInfo areaInfo;
    private SectionInfo sectionInfo;

    public VillageHeadResponseDto(
            Long id,
            String appUserId,
            String appUserName,
            String sectionName,
            Long farmerCount,
            Long areaId,
            Double areaLongitude,
            Double areaLatitude,
            String areaName,
            Long sectionId,
            Double sectionLongitude,
            Double sectionLatitude
    ) {
        this.id = id;
        this.appUserId = appUserId;
        this.appUserName = appUserName;
        this.sectionName = sectionName;
        this.farmerCount = farmerCount;
        this.areaInfo = new AreaInfo(areaId, areaLongitude, areaLatitude, areaName);
        this.sectionInfo = new SectionInfo(sectionId, sectionLongitude, sectionLatitude, sectionName);
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AreaInfo {
        private Long areaId;
        private Double longitude;
        private Double latitude;
        private String areaName;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SectionInfo {
        private Long sectionId;
        private Double longitude;
        private Double latitude;
        private String sectionName;
    }
}
