package com.coffee.atom.dto.appuser;

import com.coffee.atom.domain.area.Area;
import com.coffee.atom.domain.area.Section;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VillageHeadDetailResponseDto {
    private String userId;
    private String username;
    private String bankName;
    private String accountInfo;
    private String identificationPhotoUrl;
    private String contractFileUrl;
    private String bankbookPhotoUrl;
    private AreaInfo areaInfo;
    private SectionInfo sectionInfo;

    @Data
    @Builder
    public static class AreaInfo{
        private Long areaId;
        private Double longitude;
        private Double latitude;
        private String areaName;

        public static VillageHeadDetailResponseDto.AreaInfo from(Area area){
            return VillageHeadDetailResponseDto.AreaInfo.builder()
                    .areaId(area.getId())
                    .longitude(area.getLongitude())
                    .latitude(area.getLatitude())
                    .areaName(area.getAreaName())
                    .build();
        }
    }

    @Data
    @Builder
    public static class SectionInfo{
        private Long sectionId;
        private Double longitude;
        private Double latitude;
        private String sectionName;

        public static VillageHeadDetailResponseDto.SectionInfo from(Section section){
            return SectionInfo.builder()
                    .sectionId(section.getId())
                    .longitude(section.getLongitude())
                    .latitude(section.getLatitude())
                    .sectionName(section.getSectionName())
                    .build();
        }
    }
}
