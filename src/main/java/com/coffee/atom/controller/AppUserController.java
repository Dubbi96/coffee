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
    @Operation(summary = "신규 계정 생성 1️⃣ 총 관리자", description = "<b>총 관리자만 가능</b> <br>부 관리자 / 면장 생성 용도")
    public Long signUp(@LoginAppUser AppUser appUser,
                       @RequestPart("userId") String userId,
                       @RequestPart("username") String username,
                       @RequestPart("password") String password,
                       @RequestPart("role") Role role,
                       @RequestPart(value = "areaId", required = false) Long areaId,
                       @RequestPart(value = "sectionId", required = false) Long sectionId,
                       @RequestPart(value = "bankName", required = false) String bankName,
                       @RequestPart(value = "accountInfo", required = false) String accountInfo,
                       @RequestPart(value = "idCardFile", required = false) MultipartFile idCardFile,
                       @RequestPart(value = "identificationPhotoFile", required = false) MultipartFile identificationPhotoFile,
                       @RequestPart(value = "contractFile", required = false) MultipartFile contractFile,
                       @RequestPart(value = "bankbookFile", required = false) MultipartFile bankbookFile) {
        return appUserService.signUp(appUser, userId, username, password, role, areaId, sectionId,
                bankName, accountInfo, idCardFile, identificationPhotoFile, contractFile, bankbookFile);
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
    @Operation(summary = "부 관리자 정보 수정 1️⃣ 총 관리자 ", description = "<b>부 관리자 정보 수정</b> <br>총 관리자만 사용 가능 <br>수정 가능 정보: 이름, 유저아이디, 관리지역, idCard 이미지")
    public void updateViceAdmin(@PathVariable("viceAdminId") Long viceAdminId,
                                @LoginAppUser AppUser appUser,
                                @RequestPart("username") String username,
                                @RequestPart("userId") String userId,
                                @RequestPart("areaId") Long areaId,
                                @RequestPart(value = "idCardFile", required = false) MultipartFile idCardFile) {
        appUserService.updateViceAdmin(viceAdminId, appUser, username, userId, areaId, idCardFile);
    }

    //TODO: 3. 내 정보 조회
}
