package com.coffee.atom.dto.area;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AreaDto {
    private Long id;
    private String areaName;
    private Double longitude;
    private Double latitude;
}