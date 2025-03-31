package com.coffee.atom.dto.approval;

import com.coffee.atom.domain.approval.Approval;
import com.coffee.atom.domain.approval.Method;
import com.coffee.atom.domain.approval.ServiceType;
import com.coffee.atom.domain.approval.Status;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class ApprovalResponseDto {
    private Long id;
    private String requesterName;
    private String approverName;
    private Status status;
    private Method method;
    private ServiceType serviceType;
    private LocalDateTime createdAt;

    public static ApprovalResponseDto from(Approval approval) {
        return ApprovalResponseDto.builder()
                .id(approval.getId())
                .requesterName(approval.getRequester().getUsername())
                .approverName(approval.getApprover().getUsername())
                .status(approval.getStatus())
                .method(approval.getMethod())
                .serviceType(approval.getServiceType())
                .createdAt(approval.getCreatedAt())
                .build();
    }
}