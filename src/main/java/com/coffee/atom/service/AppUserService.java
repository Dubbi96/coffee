package com.coffee.atom.service;

import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.config.security.JwtProvider;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.AppUserRepository;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.dto.appuser.AppUserResponseDto;
import com.coffee.atom.dto.appuser.SignInRequestDto;
import com.coffee.atom.dto.appuser.SignInResponseDto;
import com.coffee.atom.dto.appuser.SignUpRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Transactional(readOnly = true)
    public SignInResponseDto login(SignInRequestDto accountRequestDto) {
        AppUser appUser = appUserRepository.findByUserId(accountRequestDto.getUserId())
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage()));
        if (!passwordEncoder.matches(accountRequestDto.getPassword() + appUser.getSalt(), appUser.getPassword()))
            throw new CustomException("올바르지 않은 아이디 및 비밀번호입니다.");
        return new SignInResponseDto(appUser, jwtProvider.createAccessToken(appUser.getId()));
    }

    @Transactional
    public Long signUp(SignUpRequestDto authRequestDto) {
        appUserRepository.findByUsername(authRequestDto.getUserId()).ifPresent(appUser -> {
            throw new IllegalArgumentException(ErrorValue.NICKNAME_ALREADY_EXISTS.toString());
        });
        String salt = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(authRequestDto.getPassword() + salt);

        Role userRole = Role.valueOf(authRequestDto.getRole().toUpperCase()); // String을 Role enum으로 변환

        AppUser newUser = AppUser.builder()
                .userId(authRequestDto.getUserId())
                .username(authRequestDto.getUsername())
                .password(encodedPassword)
                .salt(salt)
                .role(userRole)
                .build();

        appUserRepository.save(newUser);
        return newUser.getId();
    }

    @Transactional(readOnly = true)
    public AppUserResponseDto getUserDetails(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorValue.ACCOUNT_NOT_FOUND.getMessage()));
        return new AppUserResponseDto(user);
    }

    @Transactional(readOnly = true)
    public List<AppUserResponseDto> getUserList() {
        return appUserRepository.findAll()
                .stream()
                .sorted(Comparator.comparing((AppUser user) -> user.getRole().ordinal()) // Role Enum에 표기된 순서대로 나열
                        .thenComparing(AppUser::getId)) // 같은 역할 내에서 appUserId 오름차순 정렬
                .map(AppUserResponseDto::new)
                .toList();
    }

    @Transactional
    public void updateAppUserPassword(AppUser appUser, String newPassword) {
        String salt = UUID.randomUUID().toString();
        String encodedPassword = passwordEncoder.encode(newPassword + salt);
        appUser.updatePassword(encodedPassword, salt);
        appUserRepository.save(appUser);
    }

}