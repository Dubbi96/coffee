package com.coffee.atom.controller;

import com.coffee.atom.config.security.LoginAppUser;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.dto.appuser.*;
import com.coffee.atom.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/app-user")
@RequiredArgsConstructor
public class AppUserController {
    private final AppUserService appUserService;

    @PostMapping("/sign-in")
    public SignInResponseDto login(@RequestBody SignInRequestDto authRequest) {
        log.info("로그인 요청 받음: {}", authRequest.getUserId());
        return appUserService.login(authRequest);
    }

    @PostMapping("/sign-up")
    @Operation(summary = "신규 계정 생성 1️⃣ 총 관리자", description = "<b>신규 ID 즉시 생성됨</b>")
    public Long signup(@RequestBody SignUpRequestDto signUpRequestDto) {
        log.info("회원가입 요청 받음: {}", signUpRequestDto);
        return appUserService.signUp(signUpRequestDto);
    }

    @PatchMapping("/")
    @Operation(summary = "유저 정보 수정", description = "<b>단일 유저 정보 수정</b> <br> 모든 유저 Role 관계 없이 공통으로 사용 가능 <br>**ADMIN, VILLAGE_HEAD 일 경우** : 유저명, password만 입력 idCardUrl은 null <br>**VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 일 경우** : idCardUrl까지 첨부 가능")
    public void updateUserStatus(@LoginAppUser AppUser appUser, @RequestBody AppUserStatusUpdateRequestDto appUserStatusUpdateRequestDto) {
        appUserService.updateAppUserStatus(appUser, appUserStatusUpdateRequestDto);
    }

    @GetMapping("/village-heads")
    @Operation(summary = "면장 목록 조회 1️⃣ 총 관리자 2️⃣ 부 관리자 ", description = "<b>면장 목록 조회</b> <br> **총 관리자**로 조회할 경우 면장 전체 목록 조회 <br> **부 관리자**로 조회할 경우 해당 부 관리자가 관리하고 있는 지역의 면장들만 조회")
    public List<VillageHeadResponseDto> getVillageHeads(@LoginAppUser AppUser appUser) {
        return appUserService.getVillageHeads(appUser);
    }
}
