package com.coffee.atom.dto.approval;

import com.coffee.atom.domain.approval.Method;
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
public class VillageHeadDetailResponseDto implements ApprovalDetailResponse{
    private Long id;
    private Long requesterId;
    private String requesterName;
    private String userId;
    private String bankName;
    private String username;
    private String accountInfo;
    private Long areaId;
    private String areaName;
    private Long sectionId;
    private String sectionName;
    private String contractFileUrl;
    private String bankbookPhotoUrl;
    private String identificationPhotoUrl;
    private Status status;
    private ServiceType serviceType;
    private String rejectedReason;
    private Method method;
}
