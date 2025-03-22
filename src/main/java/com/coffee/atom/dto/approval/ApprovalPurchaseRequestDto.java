package com.coffee.atom.dto.approval;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ApprovalPurchaseRequestDto {
    private Long id;
    private Long deduction;
    private Long paymentAmount;
    private LocalDateTime purchaseDate;
    private Long quantity;
    private Long totalPrice;
    private Long unitPrice;
    private Long managerId;
}
