package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.domain.area.Area;
import com.coffee.atom.domain.area.AreaRepository;
import com.coffee.atom.dto.area.AreaRequestDto;
import com.coffee.atom.dto.area.AreaResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static com.coffee.atom.support.TestFixtures.area;
import static com.coffee.atom.support.TestFixtures.user;
import static com.coffee.atom.support.TestFixtures.viceAdmin;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AreaServiceTest {

    @Mock
    AreaRepository areaRepository;

    @InjectMocks
    AreaService areaService;

    @Test
    void saveArea_nonAdmin_throws() {
        AppUser requester = user(1L, Role.VILLAGE_HEAD);
        AreaRequestDto dto = new AreaRequestDto();
        dto.setAreaName("A");
        dto.setLatitude(1.0);
        dto.setLongitude(2.0);

        assertThatThrownBy(() -> areaService.saveArea(requester, dto))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.UNAUTHORIZED.getMessage());
    }

    @Test
    void saveArea_admin_saves() {
        AppUser requester = user(1L, Role.ADMIN);
        AreaRequestDto dto = new AreaRequestDto();
        dto.setAreaName("A");
        dto.setLatitude(1.0);
        dto.setLongitude(2.0);

        areaService.saveArea(requester, dto);

        verify(areaRepository).save(any(Area.class));
    }

    @Test
    void getMyAreaForViceAdmin_wrongRole_throws() {
        AppUser requester = user(1L, Role.ADMIN);

        assertThatThrownBy(() -> areaService.getMyAreaForViceAdmin(requester))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.UNAUTHORIZED_SERVICE.getMessage());
    }

    @Test
    void getMyAreaForViceAdmin_noArea_throws() {
        AppUser requester = viceAdmin(1L, Role.VICE_ADMIN_HEAD_OFFICER, null);

        assertThatThrownBy(() -> areaService.getMyAreaForViceAdmin(requester))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND.getMessage());
    }

    @Test
    void getAreaById_notFound_throws() {
        when(areaRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> areaService.getAreaById(1L))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.SUBJECT_NOT_FOUND.getMessage());
    }

    @Test
    void getMyAreaForViceAdmin_success() {
        Area a = area(10L);
        AppUser requester = viceAdmin(1L, Role.VICE_ADMIN_HEAD_OFFICER, a);

        AreaResponseDto dto = areaService.getMyAreaForViceAdmin(requester);

        assertThat(dto.getId()).isEqualTo(10L);
    }
}


