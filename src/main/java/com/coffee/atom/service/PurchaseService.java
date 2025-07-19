package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.Purchase;
import com.coffee.atom.domain.PurchaseRepository;
import com.coffee.atom.domain.appuser.*;
import com.coffee.atom.domain.area.Area;
import com.coffee.atom.dto.PurchaseResponseDto;
import com.coffee.atom.dto.approval.ApprovalPurchaseRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.coffee.atom.domain.appuser.QViceAdminDetail.viceAdminDetail;

@Service
@RequiredArgsConstructor
public class PurchaseService {
    private final PurchaseRepository purchaseRepository;
    private final VillageHeadDetailRepository villageHeadDetailRepository;
    private final AppUserRepository appUserRepository;
    private final ViceAdminDetailRepository viceAdminDetailRepository;

    @Transactional
    public ApprovalPurchaseRequestDto requestApprovalToCreatePurchase(AppUser requester, ApprovalPurchaseRequestDto approvalPurchaseRequestDto) {
        if (requester.getRole() != Role.VICE_ADMIN_HEAD_OFFICER && requester.getRole() != Role.ADMIN) throw new CustomException(ErrorValue.UNAUTHORIZED.getMessage());
        Purchase purchase = Purchase.builder()
                .manager(requester)
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

    @Transactional(readOnly = true)
    public List<PurchaseResponseDto> getPurchaseList(AppUser appUser) {
        return switch (appUser.getRole()) {
            case ADMIN -> purchaseRepository.findByIsApprovedTrueOrderByPurchaseDateDesc().stream()
                    .map(PurchaseResponseDto::from)
                    .toList();

            case VICE_ADMIN_HEAD_OFFICER -> purchaseRepository.findByIsApprovedTrueAndManager_IdOrderByPurchaseDateDesc(appUser.getId()).stream()
                    .map(PurchaseResponseDto::from)
                    .toList();

            case VILLAGE_HEAD -> {
                // 1. 면장 상세 정보 조회 (Section → Area 확인)
                VillageHeadDetail detail = villageHeadDetailRepository.findById(appUser.getId())
                        .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage()));
                Area area = detail.getSection().getArea();

                // 2. 해당 Area를 담당하는 ViceAdminDetail 조회
                List<ViceAdminDetail> viceAdmins = viceAdminDetailRepository.findByAreaAndAppUser_Role(area, Role.VICE_ADMIN_HEAD_OFFICER);

                if (viceAdmins.isEmpty()) {
                    throw new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage());
                }

                // 예: 첫 번째 부관리자 선택 (기준 필요 시 정렬 후 선택)
                ViceAdminDetail selectedViceAdmin = viceAdmins.get(0);
                Long viceAdminId = selectedViceAdmin.getId();

                // 3. 해당 부관리자의 구매 내역 조회
                yield purchaseRepository.findByIsApprovedTrueAndManager_IdOrderByPurchaseDateDesc(viceAdminId).stream()
                        .map(PurchaseResponseDto::from)
                        .toList();
            }

            case VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER ->
                    throw new CustomException(ErrorValue.UNAUTHORIZED_SERVICE.getMessage());
        };
    }
}
