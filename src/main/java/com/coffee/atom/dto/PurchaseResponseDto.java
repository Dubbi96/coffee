package com.coffee.atom.dto;

import com.coffee.atom.domain.Purchase;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class PurchaseResponseDto {
    private Long id;
    private Long managerId;
    private String managerName;
    private LocalDate purchaseDate;
    private Long quantity;
    private Long unitPrice;
    private Long totalPrice;
    private Long deduction;
    private Long paymentAmount;

    public static PurchaseResponseDto from(Purchase purchase) {
        return PurchaseResponseDto.builder()
                .id(purchase.getId())
                .managerId(purchase.getManager().getId())
                .managerName(purchase.getManager().getUsername())
                .purchaseDate(purchase.getPurchaseDate())
                .quantity(purchase.getQuantity())
                .unitPrice(purchase.getUnitPrice())
                .totalPrice(purchase.getTotalPrice())
                .deduction(purchase.getDeduction())
                .paymentAmount(purchase.getPaymentAmount())
                .build();
    }
}