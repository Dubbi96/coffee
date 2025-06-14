package com.coffee.atom.dto.appuser;

import com.coffee.atom.domain.appuser.ViceAdminDetail;
import com.coffee.atom.domain.area.Area;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViceAdminsResponseDto {
    private Long id;
    private String userName;
    private String userId;
    private AreaInfo areaInfo;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AreaInfo {
        private Long areaId;
        private String areaName;

        public static AreaInfo from(Area area) {
            return AreaInfo.builder()
                    .areaId(area.getId())
                    .areaName(area.getAreaName())
                    .build();
        }
    }

    public static ViceAdminsResponseDto from(ViceAdminDetail detail) {
        return ViceAdminsResponseDto.builder()
                .id(detail.getId())
                .userName(detail.getAppUser().getUsername())
                .userId(detail.getAppUser().getUserId())
                .areaInfo(AreaInfo.from(detail.getArea()))
                .build();
    }
}