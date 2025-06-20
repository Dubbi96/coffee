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
public class SectionDetailResponseDto implements ApprovalDetailResponse {
    private Long id;
    private Long requesterId;
    private String requesterName;
    private Long areaId;
    private Double latitude;
    private Double longitude;
    private String sectionName;
    private Status status;
    private ServiceType serviceType;
    private String rejectedReason;
    private Method method;
}