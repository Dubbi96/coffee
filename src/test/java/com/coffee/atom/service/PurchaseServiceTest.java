package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.Purchase;
import com.coffee.atom.domain.PurchaseRepository;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.AppUserRepository;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.dto.PurchaseResponseDto;
import com.coffee.atom.dto.approval.ApprovalPurchaseRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.coffee.atom.support.TestFixtures.area;
import static com.coffee.atom.support.TestFixtures.section;
import static com.coffee.atom.support.TestFixtures.viceAdmin;
import static com.coffee.atom.support.TestFixtures.villageHead;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseServiceTest {

    @Mock
    PurchaseRepository purchaseRepository;

    @Mock
    AppUserRepository appUserRepository;

    @InjectMocks
    PurchaseService purchaseService;

    @Test
    void requestApprovalToCreatePurchase_unauthorizedRole_throws() {
        AppUser requester = viceAdmin(1L, Role.VILLAGE_HEAD, null);
        ApprovalPurchaseRequestDto dto = new ApprovalPurchaseRequestDto();

        assertThatThrownBy(() -> purchaseService.requestApprovalToCreatePurchase(requester, dto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.UNAUTHORIZED.getMessage());
    }

    @Test
    void requestApprovalToCreatePurchase_villageHeadNotApproved_throws() {
        AppUser requester = viceAdmin(1L, Role.ADMIN, null);
        var a = area(1L);
        var s = section(2L, a, true);
        AppUser villageHead = villageHead(10L, s, false);

        ApprovalPurchaseRequestDto dto = new ApprovalPurchaseRequestDto();
        dto.setVillageHeadId(10L);
        dto.setPurchaseDate(LocalDate.of(2025, 1, 1));
        dto.setQuantity(1L);
        dto.setUnitPrice(10L);
        dto.setTotalPrice(10L);
        dto.setDeduction(0L);
        dto.setPaymentAmount(10L);

        when(appUserRepository.findById(10L)).thenReturn(Optional.of(villageHead));

        assertThatThrownBy(() -> purchaseService.requestApprovalToCreatePurchase(requester, dto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.VILLAGE_HEAD_NOT_APPROVED.getMessage());
    }

    @Test
    void requestApprovalToCreatePurchase_viceAdminAreaMismatch_throws() {
        var viceArea = area(1L);
        AppUser requester = viceAdmin(1L, Role.VICE_ADMIN_HEAD_OFFICER, viceArea);

        var otherArea = area(2L);
        var otherSection = section(3L, otherArea, true);
        AppUser villageHead = villageHead(10L, otherSection, true);

        ApprovalPurchaseRequestDto dto = new ApprovalPurchaseRequestDto();
        dto.setVillageHeadId(10L);
        dto.setPurchaseDate(LocalDate.of(2025, 1, 1));
        dto.setQuantity(1L);
        dto.setUnitPrice(10L);
        dto.setTotalPrice(10L);
        dto.setDeduction(0L);
        dto.setPaymentAmount(10L);

        when(appUserRepository.findById(10L)).thenReturn(Optional.of(villageHead));

        assertThatThrownBy(() -> purchaseService.requestApprovalToCreatePurchase(requester, dto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.VILLAGE_HEAD_AREA_MISMATCH.getMessage());
    }

    @Test
    void requestApprovalToCreatePurchase_success_setsDtoId() {
        AppUser requester = viceAdmin(1L, Role.ADMIN, null);
        var a = area(1L);
        var s = section(2L, a, true);
        AppUser villageHead = villageHead(10L, s, true);

        ApprovalPurchaseRequestDto dto = new ApprovalPurchaseRequestDto();
        dto.setVillageHeadId(10L);
        dto.setPurchaseDate(LocalDate.of(2025, 1, 1));
        dto.setQuantity(1L);
        dto.setUnitPrice(10L);
        dto.setTotalPrice(10L);
        dto.setDeduction(0L);
        dto.setPaymentAmount(10L);

        when(appUserRepository.findById(10L)).thenReturn(Optional.of(villageHead));
        when(purchaseRepository.save(any(Purchase.class))).thenAnswer(inv -> {
            Purchase p = inv.getArgument(0);
            // JPA save 후 같은 엔티티 인스턴스에 id가 생기는 상황을 흉내
            ReflectionTestUtils.setField(p, "id", 99L);
            return p;
        });

        ApprovalPurchaseRequestDto result = purchaseService.requestApprovalToCreatePurchase(requester, dto);

        // 서비스 로직상 dto.setId(purchase.getId())를 호출하므로, save mock이 id를 제공해야 함
        assertThat(result.getId()).isEqualTo(99L);
        verify(purchaseRepository).save(any(Purchase.class));
    }

    @Test
    void getPurchaseList_callsRepositoryAndMaps() {
        AppUser admin = viceAdmin(1L, Role.ADMIN, null);
        var a = area(1L);
        var s = section(2L, a, true);
        AppUser vh = villageHead(10L, s, true);
        Purchase p = com.coffee.atom.support.TestFixtures.purchase(1L, admin, vh, true);

        when(purchaseRepository.findAll(org.mockito.ArgumentMatchers.<Specification<Purchase>>any(), eq(Sort.by(Sort.Direction.DESC, "purchaseDate"))))
                .thenReturn(List.of(p));

        List<PurchaseResponseDto> result = purchaseService.getPurchaseList(admin, null, null, null);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void getPurchaseList_viceAdmin_withoutArea_throws() {
        AppUser viceAdmin = viceAdmin(1L, Role.VICE_ADMIN_HEAD_OFFICER, null);

        assertThatThrownBy(() -> purchaseService.getPurchaseList(viceAdmin, null, null, null))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND.getMessage());
    }
}


