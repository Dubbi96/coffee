package com.coffee.atom.dto.area;

import lombok.Data;

@Data
public class AreaRequestDto {
    private String areaName;
    private Double latitude;
    private Double longitude;
}
