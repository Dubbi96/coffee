package com.coffee.atom.service.approval;

import com.coffee.atom.domain.FarmerRepository;
import com.coffee.atom.domain.PurchaseRepository;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.AppUserRepository;
import com.coffee.atom.domain.area.SectionRepository;
import com.coffee.atom.dto.approval.ApprovalSectionRequestDto;
import com.coffee.atom.service.AppUserService;
import com.coffee.atom.service.PurchaseService;
import com.coffee.atom.service.SectionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.coffee.atom.support.TestFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalFacadeServiceTest {

    @Mock AppUserService appUserService;
    @Mock ApprovalService approvalService;
    @Mock PurchaseService purchaseService;
    @Mock SectionService sectionService;
    @Mock FarmerRepository farmerRepository;
    @Mock SectionRepository sectionRepository;
    @Mock PurchaseRepository purchaseRepository;
    @Mock AppUserRepository appUserRepository;

    @InjectMocks
    ApprovalFacadeService approvalFacadeService;

    @Test
    void processSectionCreation_callsSectionServiceThenApprovalService() throws Exception {
        AppUser requester = user(1L, com.coffee.atom.domain.appuser.Role.ADMIN);
        Long approverId = 99L;
        ApprovalSectionRequestDto dto = new ApprovalSectionRequestDto();
        dto.setId(123L);

        when(sectionService.requestApprovalToCreateSection(requester, dto)).thenReturn(dto);

        approvalFacadeService.processSectionCreation(requester, approverId, dto);

        ArgumentCaptor<Object> requestDtoCaptor = ArgumentCaptor.forClass(Object.class);
        ArgumentCaptor<List> refsCaptor = ArgumentCaptor.forClass(List.class);

        verify(approvalService).requestApproval(
                org.mockito.ArgumentMatchers.eq(requester),
                org.mockito.ArgumentMatchers.eq(approverId),
                requestDtoCaptor.capture(),
                org.mockito.ArgumentMatchers.eq(com.coffee.atom.domain.approval.Method.CREATE),
                org.mockito.ArgumentMatchers.eq(com.coffee.atom.domain.approval.ServiceType.SECTION),
                refsCaptor.capture()
        );

        assertThat(requestDtoCaptor.getValue()).isSameAs(dto);
        assertThat(refsCaptor.getValue()).hasSize(1);
    }
}


