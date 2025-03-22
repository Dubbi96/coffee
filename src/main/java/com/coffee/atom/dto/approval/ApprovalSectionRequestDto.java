package com.coffee.atom.dto.approval;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.Data;

@Data
public class ApprovalSectionRequestDto {
    @Parameter(description = "지역의 경도")
    private Double longitude;
    @Parameter(description = "지역의 위도")
    private Double latitude;
    @Parameter(description = "지역 명")
    private String sectionName;
}
