package com.coffee.atom.dto.area;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AreaRequestDto {
    @NotBlank(message = "지역명은 필수입니다")
    @Size(max = 100, message = "지역명은 100자 이하여야 합니다")
    private String areaName;
    
    @NotNull(message = "위도는 필수입니다")
    private Double latitude;
    
    @NotNull(message = "경도는 필수입니다")
    private Double longitude;
}
