package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.Purchase;
import com.coffee.atom.domain.PurchaseRepository;
import com.coffee.atom.domain.appuser.*;
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
    private final AppUserRepository appUserRepository;

    @Transactional
    public ApprovalPurchaseRequestDto requestApprovalToCreatePurchase(AppUser requester, ApprovalPurchaseRequestDto approvalPurchaseRequestDto) {
        // 부관리자만 Purchase 생성 가능
        if (requester.getRole() != Role.VICE_ADMIN_HEAD_OFFICER && 
            requester.getRole() != Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER && 
            requester.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

        // 면장 조회 및 검증
        AppUser villageHead = appUserRepository.findById(approvalPurchaseRequestDto.getVillageHeadId())
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND));
        
        if (villageHead.getRole() != Role.VILLAGE_HEAD || 
            villageHead.getIsApproved() == null || 
            !villageHead.getIsApproved()) {
            throw new CustomException(ErrorValue.VILLAGE_HEAD_NOT_APPROVED);
        }

        // 부관리자의 경우 본인이 배정된 지역의 면장인지 확인
        if (requester.getRole() == Role.VICE_ADMIN_HEAD_OFFICER || 
            requester.getRole() == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
            if (requester.getArea() == null) {
                throw new CustomException(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND);
            }
            if (villageHead.getSection() == null || 
                villageHead.getSection().getArea() == null ||
                !villageHead.getSection().getArea().getId().equals(requester.getArea().getId())) {
                throw new CustomException(ErrorValue.VILLAGE_HEAD_AREA_MISMATCH);
            }
        }

        Purchase purchase = Purchase.builder()
                .manager(requester)
                .villageHead(villageHead)
                .purchaseDate(approvalPurchaseRequestDto.getPurchaseDate())
                .quantity(approvalPurchaseRequestDto.getQuantity())
                .unitPrice(approvalPurchaseRequestDto.getUnitPrice())
                .totalPrice(approvalPurchaseRequestDto.getTotalPrice())
                .deduction(approvalPurchaseRequestDto.getDeduction())
                .paymentAmount(approvalPurchaseRequestDto.getPaymentAmount())
                .remarks(approvalPurchaseRequestDto.getRemarks())
                .build();
        purchaseRepository.save(purchase);
        approvalPurchaseRequestDto.setId(purchase.getId());
        return approvalPurchaseRequestDto;
    }

    @Transactional
    public ApprovalPurchaseRequestDto requestApprovalToUpdatePurchase(AppUser requester, ApprovalPurchaseRequestDto approvalPurchaseRequestDto) {
        // 부관리자만 Purchase 수정 가능
        if (requester.getRole() != Role.VICE_ADMIN_HEAD_OFFICER && 
            requester.getRole() != Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER && 
            requester.getRole() != Role.ADMIN) {
            throw new CustomException(ErrorValue.UNAUTHORIZED);
        }

        // 기존 Purchase 조회 및 검증
        Purchase purchase = purchaseRepository.findById(approvalPurchaseRequestDto.getId())
                .orElseThrow(() -> new CustomException(ErrorValue.PURCHASE_NOT_FOUND));

        // 부관리자의 경우 본인이 관리하는 Purchase만 수정 가능
        if (requester.getRole() == Role.VICE_ADMIN_HEAD_OFFICER || 
            requester.getRole() == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) {
            if (requester.getArea() == null) {
                throw new CustomException(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND);
            }
            if (!purchase.getManager().getId().equals(requester.getId())) {
                throw new CustomException(ErrorValue.UNAUTHORIZED_SERVICE);
            }
        }

        // 면장 조회 및 검증
        AppUser villageHead = appUserRepository.findById(approvalPurchaseRequestDto.getVillageHeadId())
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND));
        
        if (villageHead.getRole() != Role.VILLAGE_HEAD || 
            villageHead.getIsApproved() == null ||
                Boolean.TRUE.equals(!villageHead.getIsApproved())) {
            throw new CustomException(ErrorValue.VILLAGE_HEAD_NOT_APPROVED);
        }

        // 부관리자의 경우 본인이 배정된 지역의 면장인지 확인
        if ((requester.getRole() == Role.VICE_ADMIN_HEAD_OFFICER ||
            requester.getRole() == Role.VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER) && (villageHead.getSection() == null ||
                villageHead.getSection().getArea() == null ||
                !villageHead.getSection().getArea().getId().equals(requester.getArea().getId()))) {
                throw new CustomException(ErrorValue.VILLAGE_HEAD_AREA_MISMATCH);
            }


        // Purchase 정보 업데이트 (승인 전까지는 임시 저장)
        purchase.updateManager(requester);
        purchase.updateVillageHead(villageHead);
        purchase.updatePurchaseDate(approvalPurchaseRequestDto.getPurchaseDate());
        purchase.updateQuantity(approvalPurchaseRequestDto.getQuantity());
        purchase.updateUnitPrice(approvalPurchaseRequestDto.getUnitPrice());
        purchase.updateTotalPrice(approvalPurchaseRequestDto.getTotalPrice());
        purchase.updateDeduction(approvalPurchaseRequestDto.getDeduction());
        purchase.updatePaymentAmount(approvalPurchaseRequestDto.getPaymentAmount());
        purchase.updateRemarks(approvalPurchaseRequestDto.getRemarks());
        
        purchaseRepository.save(purchase);
        return approvalPurchaseRequestDto;
    }

    @Transactional(readOnly = true)
    public List<PurchaseResponseDto> getPurchaseList(AppUser appUser) {
        return switch (appUser.getRole()) {
            case ADMIN -> purchaseRepository.findByIsApprovedTrueOrderByPurchaseDateDesc().stream()
                    .map(PurchaseResponseDto::from)
                    .toList();

            case VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER -> 
                    purchaseRepository.findByIsApprovedTrueAndManager_IdOrderByPurchaseDateDesc(appUser.getId()).stream()
                    .map(PurchaseResponseDto::from)
                    .toList();

            case VILLAGE_HEAD -> 
                    // 면장은 본인과 1:1 관계인 Purchase만 조회
                    purchaseRepository.findByIsApprovedTrueAndVillageHead_IdOrderByPurchaseDateDesc(appUser.getId()).stream()
                    .map(PurchaseResponseDto::from)
                    .toList();
        };
    }
}
