package com.coffee.atom.dto.area;

import com.coffee.atom.domain.area.Section;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AreaSectionResponseDto {
    private Long id;
    private String areaName;
    private Double latitude;
    private Double longitude;
    private List<Sections> sections;

    @Data
    @Builder
    public static class Sections{
        private Long id;
        private Double longitude;
        private Double latitude;
        private String sectionName;

        public static AreaSectionResponseDto.Sections from(Section section){
            return AreaSectionResponseDto.Sections.builder()
                    .id(section.getId())
                    .longitude(section.getLongitude())
                    .latitude(section.getLatitude())
                    .sectionName(section.getSectionName())
                    .build();
        }
    }
}
