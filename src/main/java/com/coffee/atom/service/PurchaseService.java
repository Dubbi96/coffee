package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.Purchase;
import com.coffee.atom.domain.PurchaseRepository;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.dto.PurchaseResponseDto;
import com.coffee.atom.dto.approval.ApprovalPurchaseRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;

    @Transactional
    public ApprovalPurchaseRequestDto requestApprovalToCreatePurchase(AppUser requester, ApprovalPurchaseRequestDto approvalPurchaseRequestDto) {
        if(requester.getRole() != Role.VICE_ADMIN_HEAD_OFFICER || requester.getRole() != Role.ADMIN) throw new CustomException(ErrorValue.UNAUTHORIZED.getMessage());
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
        approvalPurchaseRequestDto.setId(purchase.getId());
        return approvalPurchaseRequestDto;
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponseDto> getPurchaseList(AppUser appUser) {
        return switch (appUser.getRole()) {
            case ADMIN -> purchaseRepository.findByIsApprovedTrueOrderByPurchaseDateDesc().stream()
                    .map(PurchaseResponseDto::from)
                    .toList();

            case VICE_ADMIN_HEAD_OFFICER -> purchaseRepository.findByIsApprovedTrueAndManager_IdOrderByPurchaseDateDesc(appUser.getId()).stream()
                    .map(PurchaseResponseDto::from)
                    .toList();

            case VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER, VILLAGE_HEAD ->
                    throw new CustomException(ErrorValue.UNAUTHORIZED_SERVICE.getMessage());
        };
    }
}
