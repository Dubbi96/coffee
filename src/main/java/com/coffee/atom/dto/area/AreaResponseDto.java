package com.coffee.atom.dto.area;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AreaResponseDto {
    private Long id;
    private String areaName;
    private Double latitude;
    private Double longitude;
}
