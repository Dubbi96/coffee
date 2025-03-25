package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.Purchase;
import com.coffee.atom.domain.PurchaseRepository;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.dto.approval.ApprovalPurchaseRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;

    @Transactional
    public Long requestApprovalToCreatePurchase(AppUser requester, ApprovalPurchaseRequestDto approvalPurchaseRequestDto) {
        if(requester.getRole() != Role.VICE_ADMIN_HEAD_OFFICER) throw new CustomException(ErrorValue.UNAUTHORIZED.getMessage());

        Purchase purchase = Purchase.builder()
                .manager(requester)
                .purchaseDate(approvalPurchaseRequestDto.getPurchaseDate())
                .quantity(approvalPurchaseRequestDto.getQuantity())
                .unitPrice(approvalPurchaseRequestDto.getUnitPrice())
                .totalPrice(approvalPurchaseRequestDto.getTotalPrice())
                .deduction(approvalPurchaseRequestDto.getDeduction())
                .paymentAmount(approvalPurchaseRequestDto.getPaymentAmount())
                .build();
        purchaseRepository.save(purchase);
        return purchase.getId();
    }
}
