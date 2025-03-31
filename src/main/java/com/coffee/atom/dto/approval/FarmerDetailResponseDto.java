package com.coffee.atom.dto.approval;

import com.coffee.atom.domain.approval.ServiceType;
import com.coffee.atom.domain.approval.Status;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FarmerDetailResponseDto implements ApprovalDetailResponse {
    private Long id;
    private String name;
    private Long villageHeadId;
    private String identificationPhotoUrl;
    private Status status;
    private ServiceType serviceType;
    private String rejectedReason;
}