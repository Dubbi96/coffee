package com.coffee.atom.dto.appuser;

import com.coffee.atom.domain.Section;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VillageHeadDetailResponseDto {
    private String userId;
    private String username;
    private String accountInfo;
    private String identificationPhotoUrl;
    private String contractFileUrl;
    private String bankbookPhotoUrl;
    private SectionInfo sectionInfo;

    @Data
    @Builder
    public static class SectionInfo{
        private Double longitude;
        private Double latitude;
        private String sectionName;

        public static VillageHeadDetailResponseDto.SectionInfo from(Section section){
            return VillageHeadDetailResponseDto.SectionInfo.builder()
                    .longitude(section.getLongitude())
                    .latitude(section.getLatitude())
                    .sectionName(section.getSectionName())
                    .build();
        }
    }
}
