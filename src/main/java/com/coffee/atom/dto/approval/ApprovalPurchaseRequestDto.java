package com.coffee.atom.dto.approval;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ApprovalPurchaseRequestDto {
    private Long id;
    private Long villageHeadId; // 면장 ID (1:1 관계)
    private Long deduction;
    private Long paymentAmount;
    private LocalDate purchaseDate;
    private Long quantity;
    private Long totalPrice;
    private Long unitPrice;
    private String remarks;
}
