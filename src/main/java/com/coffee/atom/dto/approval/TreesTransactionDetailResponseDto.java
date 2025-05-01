package com.coffee.atom.dto.approval;

import com.coffee.atom.domain.approval.ServiceType;
import com.coffee.atom.domain.approval.Status;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TreesTransactionDetailResponseDto implements ApprovalDetailResponse {
    private Long id;
    private String species;
    private Long farmerId;
    private Long quantity;
    private String receivedDate;
    private Status status;
    private ServiceType serviceType;
    private String rejectedReason;
}