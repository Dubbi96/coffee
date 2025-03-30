package com.coffee.atom.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FarmersResponseDto {
    private Long id;
    private String farmerName;
    private String villageHeadName;
    private String sectionName;
}
