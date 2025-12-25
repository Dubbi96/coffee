package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.config.security.JwtProvider;
import com.coffee.atom.domain.FarmerRepository;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.AppUserRepository;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.domain.area.AreaRepository;
import com.coffee.atom.domain.area.SectionRepository;
import com.coffee.atom.dto.appuser.SignInRequestDto;
import com.coffee.atom.dto.appuser.SignInResponseDto;
import com.coffee.atom.dto.appuser.VillageHeadResponseDto;
import com.coffee.atom.util.GCSUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static com.coffee.atom.support.TestFixtures.area;
import static com.coffee.atom.support.TestFixtures.user;
import static com.coffee.atom.support.TestFixtures.viceAdmin;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppUserServiceTest {

    @Mock
    AppUserRepository appUserRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtProvider jwtProvider;

    @Mock
    SectionRepository sectionRepository;

    @Mock
    GCSUtil gcsUtil;

    @Mock
    FarmerRepository farmerRepository;

    @Mock
    AreaRepository areaRepository;

    @InjectMocks
    AppUserService appUserService;

    @Test
    void login_userNotFound_throws() {
        SignInRequestDto req = new SignInRequestDto();
        req.setUserId("x");
        req.setPassword("pw");
        when(appUserRepository.findByUserId("x")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> appUserService.login(req))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.ACCOUNT_NOT_FOUND.getMessage());
    }

    @Test
    void login_notApproved_throwsAccountNotFound() {
        SignInRequestDto req = new SignInRequestDto();
        req.setUserId("x");
        req.setPassword("pw");
        AppUser u = AppUser.builder()
                .id(1L)
                .userId("x")
                .username("x")
                .password("encoded")
                .salt("salt")
                .role(Role.ADMIN)
                .isApproved(Boolean.FALSE)
                .build();
        when(appUserRepository.findByUserId("x")).thenReturn(Optional.of(u));

        assertThatThrownBy(() -> appUserService.login(req))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.ACCOUNT_NOT_FOUND.getMessage());
    }

    @Test
    void login_wrongPassword_throws() {
        SignInRequestDto req = new SignInRequestDto();
        req.setUserId("x");
        req.setPassword("pw");
        AppUser u = user(1L, Role.ADMIN);
        when(appUserRepository.findByUserId("x")).thenReturn(Optional.of(
                AppUser.builder()
                        .id(u.getId())
                        .userId("x")
                        .username(u.getUsername())
                        .password("encoded")
                        .salt("salt")
                        .role(Role.ADMIN)
                        .isApproved(Boolean.TRUE)
                        .build()
        ));
        when(passwordEncoder.matches("pw" + "salt", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> appUserService.login(req))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.WRONG_PASSWORD.getMessage());
    }

    @Test
    void login_success_returnsToken() {
        SignInRequestDto req = new SignInRequestDto();
        req.setUserId("x");
        req.setPassword("pw");
        AppUser u = AppUser.builder()
                .id(7L)
                .userId("x")
                .username("name")
                .password("encoded")
                .salt("salt")
                .role(Role.ADMIN)
                .isApproved(Boolean.TRUE)
                .build();
        when(appUserRepository.findByUserId("x")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("pw" + "salt", "encoded")).thenReturn(true);
        when(jwtProvider.createAccessToken(7L)).thenReturn("token");

        SignInResponseDto res = appUserService.login(req);

        assertThat(res.getAccessToken()).isEqualTo("token");
        assertThat(res.getAppUserId()).isEqualTo(7L);
    }

    @Test
    void getVillageHeads_admin_callsAdminQuery() {
        AppUser admin = user(1L, Role.ADMIN);
        when(appUserRepository.findAllVillageHeadsWithFarmerCountForAdmin()).thenReturn(List.of());

        List<VillageHeadResponseDto> result = appUserService.getVillageHeads(admin);

        assertThat(result).isEmpty();
        verify(appUserRepository).findAllVillageHeadsWithFarmerCountForAdmin();
    }

    @Test
    void getVillageHeads_viceAdmin_withoutArea_throws() {
        AppUser va = viceAdmin(2L, Role.VICE_ADMIN_HEAD_OFFICER, null);

        assertThatThrownBy(() -> appUserService.getVillageHeads(va))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND.getMessage());
    }

    @Test
    void getVillageHeads_otherRole_throwsUnauthorized() {
        AppUser vh = user(3L, Role.VILLAGE_HEAD);

        assertThatThrownBy(() -> appUserService.getVillageHeads(vh))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.UNAUTHORIZED.getMessage());
    }

    @Test
    void getMyInfo_viceAdmin_withoutArea_throws() {
        AppUser va = viceAdmin(2L, Role.VICE_ADMIN_HEAD_OFFICER, null);

        assertThatThrownBy(() -> appUserService.getMyInfo(va))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorValue.VICE_ADMIN_INFO_NOT_FOUND.getMessage());
    }

    @Test
    void getMyInfo_viceAdmin_success_returnsViceAdminDto() {
        AppUser va = viceAdmin(2L, Role.VICE_ADMIN_HEAD_OFFICER, area(10L));

        Object result = appUserService.getMyInfo(va);

        assertThat(result.getClass().getSimpleName()).isEqualTo("ViceAdminMyInfoDto");
    }
}


