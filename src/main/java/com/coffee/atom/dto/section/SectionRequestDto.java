package com.coffee.atom.dto.section;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionRequestDto {
    private Long id;
    @Parameter(description = "섹션의 경도")
    private Double longitude;
    @Parameter(description = "섹션의 위도")
    private Double latitude;
    @Parameter(description = "섹션 명")
    private String sectionName;
    @Parameter(description = "지역 ID")
    private Long areaId;
}
