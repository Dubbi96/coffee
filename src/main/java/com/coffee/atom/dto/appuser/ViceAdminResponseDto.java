package com.coffee.atom.dto.appuser;

import com.coffee.atom.domain.area.Area;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViceAdminResponseDto {
    private Long id;
    private String userId;
    private String username;
    private String idCardUrl;
    private AreaInfo areaInfo;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AreaInfo {
        private Long areaId;
        private String areaName;
        private Double latitude;
        private Double longitude;

        public static AreaInfo from(Area area) {
            return AreaInfo.builder()
                    .areaId(area.getId())
                    .areaName(area.getAreaName())
                    .latitude(area.getLatitude())
                    .longitude(area.getLongitude())
                    .build();
        }
    }
}