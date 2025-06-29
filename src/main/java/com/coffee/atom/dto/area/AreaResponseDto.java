package com.coffee.atom.dto.area;

import com.coffee.atom.domain.area.Area;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AreaResponseDto {
    private Long id;
    private String areaName;
    private Double latitude;
    private Double longitude;

    public static AreaResponseDto from(Area area) {
        return AreaResponseDto.builder()
                .id(area.getId())
                .areaName(area.getAreaName())
                .latitude(area.getLatitude())
                .longitude(area.getLongitude())
                .build();
    }
}
