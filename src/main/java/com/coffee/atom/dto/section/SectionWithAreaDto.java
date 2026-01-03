package com.coffee.atom.dto.section;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SectionWithAreaDto {
    private Long id;
    private String areaName;
    private String sectionName;
    private Double longitude;
    private Double latitude;
}
