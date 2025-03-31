package com.coffee.atom.dto.approval;

import com.coffee.atom.domain.approval.ServiceType;
import com.coffee.atom.domain.approval.Status;

public interface ApprovalDetailResponse {
    Status getStatus();
    ServiceType getServiceType();
    String getRejectedReason();
    void setStatus(Status status);
    void setServiceType(ServiceType serviceType);
    void setRejectedReason(String rejectedReason);
}
