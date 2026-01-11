package com.coffee.atom.dto.approval;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RejectApprovalRequestDto {
    @NotBlank(message = "거절 사유는 필수입니다")
    @Size(max = 1000, message = "거절 사유는 1000자 이하여야 합니다")
    private String rejectedReason;
}
