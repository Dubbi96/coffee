package com.coffee.atom.service.approval;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.Farmer;
import com.coffee.atom.domain.FarmerRepository;
import com.coffee.atom.domain.PurchaseRepository;
import com.coffee.atom.domain.approval.*;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.AppUserRepository;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.domain.area.SectionRepository;
import com.coffee.atom.util.GCSUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.coffee.atom.support.TestFixtures.farmer;
import static com.coffee.atom.support.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @Test
    void requestApproval_adminRequester_autoCallsProcessApproval() throws JsonProcessingException {
        ApprovalRepository approvalRepository = mock(ApprovalRepository.class);
        AppUserRepository appUserRepository = mock(AppUserRepository.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        SectionRepository sectionRepository = mock(SectionRepository.class);
        FarmerRepository farmerRepository = mock(FarmerRepository.class);
        PurchaseRepository purchaseRepository = mock(PurchaseRepository.class);
        GCSUtil gcsUtil = mock(GCSUtil.class);

        ApprovalService service = Mockito.spy(new ApprovalService(
                approvalRepository,
                appUserRepository,
                objectMapper,
                sectionRepository,
                farmerRepository,
                purchaseRepository,
                gcsUtil
        ));

        AppUser requester = user(1L, Role.ADMIN);
        AppUser approver = user(2L, Role.ADMIN);

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"x\":1}");
        when(approvalRepository.save(any(Approval.class))).thenAnswer(inv -> {
            Approval a = inv.getArgument(0);
            a.setId(123L);
            return a;
        });
        doNothing().when(service).processApproval(123L, requester);

        service.requestApproval(
                requester,
                2L,
                new Object(),
                Method.CREATE,
                ServiceType.SECTION,
                List.of(new EntityReference(EntityType.SECTION, 99L))
        );

        verify(service).processApproval(123L, requester);
        verify(approvalRepository).save(any(Approval.class));
    }

    @Test
    void requestApproval_nonAdminRequester_doesNotAutoProcess() throws JsonProcessingException {
        ApprovalRepository approvalRepository = mock(ApprovalRepository.class);
        AppUserRepository appUserRepository = mock(AppUserRepository.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        SectionRepository sectionRepository = mock(SectionRepository.class);
        FarmerRepository farmerRepository = mock(FarmerRepository.class);
        PurchaseRepository purchaseRepository = mock(PurchaseRepository.class);
        GCSUtil gcsUtil = mock(GCSUtil.class);

        ApprovalService service = Mockito.spy(new ApprovalService(
                approvalRepository,
                appUserRepository,
                objectMapper,
                sectionRepository,
                farmerRepository,
                purchaseRepository,
                gcsUtil
        ));

        AppUser requester = user(1L, Role.VICE_ADMIN_HEAD_OFFICER);
        AppUser approver = user(2L, Role.ADMIN);

        when(appUserRepository.findById(2L)).thenReturn(Optional.of(approver));
        when(objectMapper.writeValueAsString(any())).thenReturn("{\"x\":1}");
        when(approvalRepository.save(any(Approval.class))).thenAnswer(inv -> {
            Approval a = inv.getArgument(0);
            a.setId(123L);
            return a;
        });

        service.requestApproval(
                requester,
                2L,
                new Object(),
                Method.CREATE,
                ServiceType.SECTION,
                List.of(new EntityReference(EntityType.SECTION, 99L))
        );

        verify(service, never()).processApproval(anyLong(), any());
    }

    @Test
    void processApproval_villageHeadApprover_throwsUnauthorized() {
        ApprovalRepository approvalRepository = mock(ApprovalRepository.class);
        AppUserRepository appUserRepository = mock(AppUserRepository.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        SectionRepository sectionRepository = mock(SectionRepository.class);
        FarmerRepository farmerRepository = mock(FarmerRepository.class);
        PurchaseRepository purchaseRepository = mock(PurchaseRepository.class);
        GCSUtil gcsUtil = mock(GCSUtil.class);

        ApprovalService service = new ApprovalService(
                approvalRepository,
                appUserRepository,
                objectMapper,
                sectionRepository,
                farmerRepository,
                purchaseRepository,
                gcsUtil
        );

        AppUser villageHead = user(10L, Role.VILLAGE_HEAD);

        assertThatThrownBy(() -> service.processApproval(1L, villageHead))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.UNAUTHORIZED.getMessage());
    }

    @Test
    void processApproval_create_farmerApprovesInstanceAndSavesApproval() {
        ApprovalRepository approvalRepository = mock(ApprovalRepository.class);
        AppUserRepository appUserRepository = mock(AppUserRepository.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        SectionRepository sectionRepository = mock(SectionRepository.class);
        FarmerRepository farmerRepository = mock(FarmerRepository.class);
        PurchaseRepository purchaseRepository = mock(PurchaseRepository.class);
        GCSUtil gcsUtil = mock(GCSUtil.class);

        ApprovalService service = new ApprovalService(
                approvalRepository,
                appUserRepository,
                objectMapper,
                sectionRepository,
                farmerRepository,
                purchaseRepository,
                gcsUtil
        );

        AppUser approver = user(1L, Role.ADMIN);
        Farmer f = farmer(5L, user(9L, Role.VILLAGE_HEAD), false);

        Approval approval = Approval.builder()
                .id(100L)
                .approver(approver)
                .requester(user(2L, Role.VICE_ADMIN_HEAD_OFFICER))
                .status(Status.PENDING)
                .method(Method.CREATE)
                .serviceType(ServiceType.FARMER)
                .requestedData("{}")
                .build();
        RequestedInstance ri = RequestedInstance.builder()
                .entityType(EntityType.FARMER)
                .instanceId(5L)
                .approval(approval)
                .build();
        approval.setRequestedInstance(List.of(ri));

        when(approvalRepository.findById(100L)).thenReturn(Optional.of(approval));
        when(farmerRepository.findById(5L)).thenReturn(Optional.of(f));

        service.processApproval(100L, approver);

        assertThat(f.getIsApproved()).isTrue();
        assertThat(approval.getStatus()).isEqualTo(Status.APPROVED);
        verify(approvalRepository).save(approval);
    }
}


