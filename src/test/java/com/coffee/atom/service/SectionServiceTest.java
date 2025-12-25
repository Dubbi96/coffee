package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.domain.area.Area;
import com.coffee.atom.domain.area.AreaRepository;
import com.coffee.atom.domain.area.Section;
import com.coffee.atom.domain.area.SectionRepository;
import com.coffee.atom.dto.approval.ApprovalSectionRequestDto;
import com.coffee.atom.dto.section.SectionRequestDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static com.coffee.atom.support.TestFixtures.area;
import static com.coffee.atom.support.TestFixtures.user;
import static com.coffee.atom.support.TestFixtures.viceAdmin;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SectionServiceTest {

    @Mock
    AreaRepository areaRepository;

    @Mock
    SectionRepository sectionRepository;

    @InjectMocks
    SectionService sectionService;

    @Test
    void requestApprovalToCreateSection_villageHead_throws() {
        AppUser requester = user(1L, Role.VILLAGE_HEAD);
        ApprovalSectionRequestDto dto = new ApprovalSectionRequestDto();

        assertThatThrownBy(() -> sectionService.requestApprovalToCreateSection(requester, dto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.UNAUTHORIZED.getMessage());
    }

    @Test
    void requestApprovalToCreateSection_admin_withoutAreaId_throws() {
        AppUser requester = user(1L, Role.ADMIN);
        ApprovalSectionRequestDto dto = new ApprovalSectionRequestDto();

        assertThatThrownBy(() -> sectionService.requestApprovalToCreateSection(requester, dto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.AREA_NOT_FOUND.getMessage());
    }

    @Test
    void requestApprovalToCreateSection_admin_success_setsDtoId() {
        AppUser requester = user(1L, Role.ADMIN);
        Area a = area(10L);

        ApprovalSectionRequestDto dto = new ApprovalSectionRequestDto();
        dto.setAreaId(10L);
        dto.setSectionName("S");
        dto.setLatitude(1.0);
        dto.setLongitude(2.0);

        when(areaRepository.findById(10L)).thenReturn(Optional.of(a));
        when(sectionRepository.save(any(Section.class))).thenAnswer(inv -> {
            Section s = inv.getArgument(0);
            ReflectionTestUtils.setField(s, "id", 99L);
            return s;
        });

        sectionService.requestApprovalToCreateSection(requester, dto);

        verify(sectionRepository).save(any(Section.class));
        org.assertj.core.api.Assertions.assertThat(dto.getId()).isEqualTo(99L);
    }

    @Test
    void requestApprovalToCreateSection_viceAdmin_withoutArea_throws() {
        AppUser requester = viceAdmin(1L, Role.VICE_ADMIN_HEAD_OFFICER, null);
        ApprovalSectionRequestDto dto = new ApprovalSectionRequestDto();

        assertThatThrownBy(() -> sectionService.requestApprovalToCreateSection(requester, dto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.AREA_NOT_FOUND.getMessage());
    }

    @Test
    void createSection_nonAdmin_throws() {
        AppUser requester = user(1L, Role.VICE_ADMIN_HEAD_OFFICER);
        SectionRequestDto dto = SectionRequestDto.builder().build();

        assertThatThrownBy(() -> sectionService.createSection(requester, dto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.UNAUTHORIZED.getMessage());
    }

    @Test
    void deleteSection_notFound_throws() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sectionService.deleteSection(1L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.SECTION_NOT_FOUND.getMessage());
    }

    @Test
    void deleteSection_found_deletes() {
        when(sectionRepository.findById(1L)).thenReturn(Optional.of(Section.builder().id(1L).sectionName("s").latitude(1.0).longitude(2.0).build()));

        sectionService.deleteSection(1L);

        verify(sectionRepository).deleteById(1L);
    }
}


