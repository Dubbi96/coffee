package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.Farmer;
import com.coffee.atom.domain.FarmerRepository;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.dto.FarmerResponseDto;
import com.coffee.atom.dto.FarmersResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.coffee.atom.support.TestFixtures.area;
import static com.coffee.atom.support.TestFixtures.section;
import static com.coffee.atom.support.TestFixtures.user;
import static com.coffee.atom.support.TestFixtures.villageHead;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FarmerServiceTest {

    @Mock
    FarmerRepository farmerRepository;

    @InjectMocks
    FarmerService farmerService;

    @Test
    void getFarmersWithVillageHeadAndSection_admin_callsAdminQuery() {
        AppUser admin = user(1L, Role.ADMIN);
        when(farmerRepository.findAllApprovedFarmersWithVillageHeadAndSection()).thenReturn(List.of());

        List<FarmersResponseDto> result = farmerService.getFarmersWithVillageHeadAndSection(admin);

        assertThat(result).isEmpty();
        verify(farmerRepository).findAllApprovedFarmersWithVillageHeadAndSection();
    }

    @Test
    void getFarmersWithVillageHeadAndSection_viceAdmin_withoutArea_throws() {
        AppUser viceAdmin = user(2L, Role.VICE_ADMIN_HEAD_OFFICER);

        assertThatThrownBy(() -> farmerService.getFarmersWithVillageHeadAndSection(viceAdmin))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.AREA_NOT_FOUND.getMessage());
    }

    @Test
    void getFarmersWithVillageHeadAndSection_viceAdmin_withArea_callsAreaQuery() {
        AppUser viceAdmin = AppUser.builder()
                .id(2L)
                .userId("u2")
                .username("u2")
                .password("pw")
                .salt("s")
                .role(Role.VICE_ADMIN_HEAD_OFFICER)
                .area(area(10L))
                .isApproved(Boolean.TRUE)
                .build();
        when(farmerRepository.findAllByAreaId(10L)).thenReturn(List.of());

        List<FarmersResponseDto> result = farmerService.getFarmersWithVillageHeadAndSection(viceAdmin);

        assertThat(result).isEmpty();
        verify(farmerRepository).findAllByAreaId(10L);
    }

    @Test
    void getFarmersWithVillageHeadAndSection_villageHead_callsVillageHeadQuery() {
        AppUser vh = user(3L, Role.VILLAGE_HEAD);
        when(farmerRepository.findAllByVillageHeadId(3L)).thenReturn(List.of());

        List<FarmersResponseDto> result = farmerService.getFarmersWithVillageHeadAndSection(vh);

        assertThat(result).isEmpty();
        verify(farmerRepository).findAllByVillageHeadId(3L);
    }

    @Test
    void getFarmerDetail_notFound_throws() {
        when(farmerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> farmerService.getFarmerDetail(1L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.FARMER_NOT_FOUND.getMessage());
    }

    @Test
    void getFarmerDetail_whenSectionIsNull_sectionNameIsNull() {
        AppUser vh = villageHead(10L, null, true);
        Farmer farmer = Farmer.builder()
                .id(1L)
                .name("kim")
                .villageHead(vh)
                .identificationPhotoUrl("url")
                .build();
        when(farmerRepository.findById(1L)).thenReturn(Optional.of(farmer));

        FarmerResponseDto dto = farmerService.getFarmerDetail(1L);

        assertThat(dto.getSectionName()).isNull();
        assertThat(dto.getFarmerName()).isEqualTo("kim");
        assertThat(dto.getIdentificationPhotoUrl()).isEqualTo("url");
    }

    @Test
    void getFarmerDetail_whenSectionExists_returnsSectionName() {
        var a = area(1L);
        var s = section(2L, a, true);
        AppUser vh = villageHead(10L, s, true);
        Farmer farmer = Farmer.builder()
                .id(1L)
                .name("kim")
                .villageHead(vh)
                .identificationPhotoUrl("url")
                .build();
        when(farmerRepository.findById(1L)).thenReturn(Optional.of(farmer));

        FarmerResponseDto dto = farmerService.getFarmerDetail(1L);

        assertThat(dto.getSectionName()).isEqualTo("section-2");
    }
}


