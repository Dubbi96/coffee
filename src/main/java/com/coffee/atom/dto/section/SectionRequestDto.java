package com.coffee.atom.dto.section;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SectionRequestDto {
    private Long id;
    
    @Parameter(description = "섹션의 경도")
    @NotNull(message = "경도는 필수입니다")
    private Double longitude;
    
    @Parameter(description = "섹션의 위도")
    @NotNull(message = "위도는 필수입니다")
    private Double latitude;
    
    @Parameter(description = "섹션 명")
    @NotBlank(message = "섹션명은 필수입니다")
    @Size(max = 100, message = "섹션명은 100자 이하여야 합니다")
    private String sectionName;
    
    @Parameter(description = "지역 ID")
    @NotNull(message = "지역 ID는 필수입니다")
    @Positive(message = "지역 ID는 양수여야 합니다")
    private Long areaId;
}
