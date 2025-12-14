package com.coffee.atom.controller;

import com.coffee.atom.config.security.LoginAppUser;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.Role;
import com.coffee.atom.dto.appuser.*;
import com.coffee.atom.service.AppUserService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/sign-up", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "신규 계정 생성 1️⃣ 총 관리자", 
        description = "<b>총 관리자만 계정 생성 가능</b><br>" +
                      "생성 가능한 역할: VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER, VILLAGE_HEAD<br>" +
                      "<b>VICE_ADMIN 생성 시 정책:</b><br>" +
                      "- 한 지역(Area)에는 각 권한(VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER)당 한 명씩만 할당 가능 (정책 1.5)<br>" +
                      "- areaId 필수 입력<br>" +
                      "<b>VILLAGE_HEAD 생성 시:</b><br>" +
                      "- sectionId 필수 입력<br>" +
                      "- 은행 정보(bankName, accountInfo) 선택 사항"
    )
    public Long signUp(
            @RequestPart(value = "idCardFile", required = false) MultipartFile idCardFile,
            @RequestPart(value = "identificationPhotoFile", required = false) MultipartFile identificationPhotoFile,
            @RequestPart(value = "contractFile", required = false) MultipartFile contractFile,
            @RequestPart(value = "bankbookFile", required = false) MultipartFile bankbookFile,
            @RequestParam("userId") String userId,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("role") String roleStr,
            @RequestParam(value = "areaId", required = false) Long areaId,
            @RequestParam(value = "sectionId", required = false) Long sectionId,
            @RequestParam(value = "bankName", required = false) String bankName,
            @RequestParam(value = "accountInfo", required = false) String accountInfo,
            @LoginAppUser AppUser appUser
    ) {
        Role role = Role.valueOf(roleStr);
        SignUpRequestDto dto = new SignUpRequestDto(
                userId,
                username,
                password,
                role,
                areaId,
                sectionId,
                bankName,
                accountInfo,
                idCardFile,
                identificationPhotoFile,
                contractFile,
                bankbookFile
        );
        return appUserService.signUp(appUser, dto);
    }


    @PatchMapping(consumes = {"multipart/form-data"})
    @Operation(summary = "내 정보 수정", description = "<b>내 정보 수정</b> <br> 모든 유저 Role 관계 없이 공통으로 사용 가능 <br>**ADMIN, VILLAGE_HEAD 일 경우** : 유저명, password만 입력 idCardUrl은 null <br>**VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 일 경우** : idCardUrl까지 첨부 가능")
    public void updateUserStatus(@LoginAppUser AppUser appUser,
                                 @RequestPart("username") String username,
                                 @RequestPart("password") String password,
                                 @RequestPart(value = "idCardFile", required = false) MultipartFile idCardFile) {
        appUserService.updateAppUserStatus(appUser, username, password, idCardFile);
    }

    @GetMapping("/village-heads")
    @Operation(summary = "면장 목록 조회 1️⃣ 총 관리자 2️⃣ 부 관리자 ", description = "<b>면장 목록 조회</b> <br> **총 관리자**로 조회할 경우 면장 전체 목록 조회 <br> **부 관리자**로 조회할 경우 해당 부 관리자가 관리하고 있는 지역의 면장들만 조회")
    public List<VillageHeadResponseDto> getVillageHeads(@LoginAppUser AppUser appUser) {
        return appUserService.getVillageHeads(appUser);
    }

    @GetMapping("/village-head/{villageHeadId}")
    @Operation(summary = "면장 상세 조회", description = "<b>면장 상세 조회</b>")
    public VillageHeadDetailResponseDto getVillageHead(@PathVariable("villageHeadId") Long villageHeadId) {
        return appUserService.getVillageHead(villageHeadId);
    }

    @GetMapping("/vice-admins")
    @Operation(summary = "부 관리자 목록 조회 1️⃣ 총 관리자", description = "<b>부 관리자 목록 조회</b> <br> 총 관리자만 전체 부 관리자 목록을 조회할 수 있음")
    public List<ViceAdminsResponseDto> getViceAdmins(@LoginAppUser AppUser appUser) {
        return appUserService.getViceAdmins(appUser);
    }

    @GetMapping("/vice-admin/{viceAdminId}")
    @Operation(summary = "부 관리자 상세 조회 1️⃣ 총 관리자", description = "<b>부 관리자 상세 조회</b> <br> 총 관리자만 접근 가능")
    public ViceAdminResponseDto getViceAdminDetail(@PathVariable("viceAdminId") Long viceAdminId,
                                                         @LoginAppUser AppUser appUser) {
        return appUserService.getViceAdminDetail(viceAdminId, appUser);
    }

    @PatchMapping(value = "/vice-admin/{viceAdminId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "부 관리자 정보 수정 1️⃣ 총 관리자", 
        description = "<b>부 관리자 정보 수정</b><br>" +
                      "총 관리자만 사용 가능<br>" +
                      "수정 가능 정보: 이름, 유저아이디, 관리지역, idCard 이미지<br>" +
                      "<b>⚠️ 지역 변경 시 정책:</b><br>" +
                      "- 새로운 지역에 이미 같은 역할의 부 관리자가 배정되어 있으면 예외 발생 (정책 1.6)<br>" +
                      "- 기존 부 관리자를 다른 사람으로 대체 가능"
    )
    public void updateViceAdmin(
            @PathVariable("viceAdminId") Long viceAdminId,
            @LoginAppUser AppUser appUser,
            @RequestParam("username") String username,
            @RequestParam("userId") String userId,
            @RequestParam("areaId") Long areaId,
            @RequestPart(value = "idCardFile", required = false) MultipartFile idCardFile
    ) {
        ViceAdminRequestDto dto = new ViceAdminRequestDto(username, userId, areaId, idCardFile);
        appUserService.updateViceAdmin(viceAdminId, appUser, dto);
    }

    @GetMapping("/my")
    @Operation(summary = "내 정보 조회", description = "<b>로그인한 유저의 정보를 Role에 따라 조회</b><br>AppUser 정보는 공통으로 포함됨")
    public Object getMyInfo(@LoginAppUser AppUser appUser) {
        return appUserService.getMyInfo(appUser);
    }
}
