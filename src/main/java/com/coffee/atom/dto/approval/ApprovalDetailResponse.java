package com.coffee.atom.dto.approval;

import com.coffee.atom.domain.approval.Method;
import com.coffee.atom.domain.approval.ServiceType;
import com.coffee.atom.domain.approval.Status;

public interface ApprovalDetailResponse {
    Status getStatus();
    void setStatus(Status status);
    void setServiceType(ServiceType serviceType);
    void setRejectedReason(String rejectedReason);
    void setMethod(Method method);
    void setRequesterId(Long requesterId);
    void setRequesterName(String requesterName);
}
