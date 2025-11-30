package com.coffee.atom.controller;

import com.coffee.atom.config.security.LoginAppUser;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.dto.area.AreaDto;
import com.coffee.atom.dto.area.AreaRequestDto;
import com.coffee.atom.dto.area.AreaResponseDto;
import com.coffee.atom.dto.area.AreaSectionResponseDto;
import com.coffee.atom.service.AreaService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/area")
@RequiredArgsConstructor
public class AreaController {
    private final AreaService areaService;

    @PostMapping()
    @Operation(
        summary = "지역 생성 1️⃣ 총 관리자",
        description = "<b>지역 명, 지역 위도, 경도로 신규 지역 생성</b><br>" +
                      "신규 지역 생성은 ADMIN 권한만 사용 가능<br>" +
                      "타 권한의 AppUser로 해당 서비스 호출 시 UNAUTHORIZED 메세지 반환"
    )
    public void saveArea(
            @LoginAppUser AppUser appUser,
            @RequestBody AreaRequestDto areaRequestDto
    ) {
        areaService.saveArea(appUser ,areaRequestDto);
    }

    @GetMapping("/with-sections")
    @Operation(
        summary = "지역 및 섹션 조회",
        description = "<b>지역 및 섹션 조회</b><br>" +
                      "정렬 기준 1: 지역은 areaName의 순서로 정렬<br>" +
                      "정렬 기준 2: 섹션또한 sectionName의 순서로 정렬<br>" +
                      "모든 사용자가 조회 가능하며, 승인된 섹션만 조회됨"
    )
    public List<AreaSectionResponseDto> getAreasWithSections(
    ) {
        return areaService.getAreasWithSections();
    }

    @GetMapping()
    @Operation(
        summary = "지역만 조회",
        description = "<b>역할(Role)에 따라 조회되는 지역 목록</b><br>" +
                      "<b>ADMIN</b>: 모든 지역 목록 조회<br>" +
                      "<b>VICE_ADMIN_HEAD_OFFICER / VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER</b>: 본인이 배정된 지역만 조회<br>" +
                      "정렬 기준: 지역은 areaName의 순서로 정렬"
    )
    public List<AreaResponseDto> getArea(
            @LoginAppUser AppUser appUser
    ) {
        return areaService.getArea(appUser);
    }

    @GetMapping("/{areaId}/with-sections")
    @Operation(
        summary = "지역 내 섹션 조회",
        description = "<b>지역 및 섹션 조회</b><br>" +
                      "정렬 기준 1: 섹션 sectionName의 순서로 정렬"
    )
    public List<AreaSectionResponseDto> getAreaWithSections(
            @PathVariable("areaId") Long areaId
    ) {
        return areaService.getAreaWithSections(areaId);
    }

    @GetMapping("/my")
    @Operation(
        summary = "내 지역 조회 2️⃣ 부 관리자",
        description = "<b>부 관리자의 배정된 지역 조회</b><br>" +
                      "VICE_ADMIN_HEAD_OFFICER, VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER 권한만 사용 가능<br>" +
                      "본인이 배정된 지역(Area) 정보를 반환하며, 배정되지 않은 경우 예외 발생"
    )
    public AreaResponseDto getMyAreaId(
            @LoginAppUser AppUser appUser
    ) {
        return areaService.getMyAreaForViceAdmin(appUser);
    }

    @GetMapping("/{areaId}")
    @Operation(
        summary = "지역 단건 조회",
        description = "<b>지역 ID를 기준으로 해당 지역 정보를 조회</b><br>" +
                      "경도, 위도, 지역명 등을 포함한 정보를 반환"
    )
    public AreaDto getAreaById(
            @PathVariable("areaId") Long areaId
    ) {
        return areaService.getAreaById(areaId);
    }

    @DeleteMapping("/{areaId}")
    @Operation(
        summary = "지역 삭제 1️⃣ 총 관리자 ",
        description = "<b>지정한 지역 ID에 해당하는 지역(Area)을 즉시 삭제</b><br>" +
                      "ADMIN 권한을 가진 사용자만 요청할 수 있으며,<br>" +
                      "타 권한 사용자가 요청할 경우 UNAUTHORIZED 예외가 발생합니다."
    )
    public void deleteArea(
            @PathVariable("areaId") Long areaId,
            @LoginAppUser AppUser appUser
    ) {
        areaService.deleteAreaById(appUser, areaId);
    }

}
