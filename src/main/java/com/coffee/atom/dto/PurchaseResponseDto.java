package com.coffee.atom.dto;

import com.coffee.atom.domain.Purchase;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PurchaseResponseDto {
    private Long managerId;
    private LocalDate purchaseDate;
    private Long quantity;
    private Long unitPrice;
    private Long totalPrice;
    private Long deduction;
    private Long paymentAmount;

    public static PurchaseResponseDto from(Purchase purchase) {
        return PurchaseResponseDto.builder()
                .managerId(purchase.getManager().getId())
                .purchaseDate(purchase.getPurchaseDate())
                .quantity(purchase.getQuantity())
                .unitPrice(purchase.getUnitPrice())
                .totalPrice(purchase.getTotalPrice())
                .deduction(purchase.getDeduction())
                .paymentAmount(purchase.getPaymentAmount())
                .build();
    }
}