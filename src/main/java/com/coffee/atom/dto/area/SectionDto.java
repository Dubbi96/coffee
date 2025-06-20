package com.coffee.atom.dto.area;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SectionDto {
    private Long id;
    private String sectionName;
    private Double longitude;
    private Double latitude;
}