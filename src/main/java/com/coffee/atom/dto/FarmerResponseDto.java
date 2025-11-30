package com.coffee.atom.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FarmerResponseDto {
    private String sectionName;
    private String farmerName;
    private String identificationPhotoUrl;
}
