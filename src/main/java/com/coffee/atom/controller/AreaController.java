package com.coffee.atom.controller;

import com.coffee.atom.config.security.LoginAppUser;
import com.coffee.atom.domain.appuser.AppUser;
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
        summary = "지역 및 섹션 조회 1️⃣ 총 관리자",
        description = "<b>지역 및 섹션 조회</b><br>" +
                      "정렬 기준 1: 지역은 areaName의 순서로 정렬<br>" +
                      "정렬 기준 2: 섹션또한 sectionName의 순서로 정렬"+
                      "신규 지역 생성은 ADMIN 권한만 사용 가능<br>" +
                      "타 권한의 AppUser로 해당 서비스 호출 시 UNAUTHORIZED 메세지 반환"
    )
    public List<AreaSectionResponseDto> getAreaWithSections(
            @LoginAppUser AppUser appUser
    ) {
        return areaService.getAreaWithSections(appUser);
    }

    @GetMapping()
    @Operation(
        summary = "지역만 조회 1️⃣ 총 관리자",
        description = "<b>지역 조회</b><br>" +
                      "정렬 기준 1: 지역은 areaName의 순서로 정렬<br>"+
                      "신규 지역 생성은 ADMIN 권한만 사용 가능<br>" +
                      "타 권한의 AppUser로 해당 서비스 호출 시 UNAUTHORIZED 메세지 반환"
    )
    public List<AreaResponseDto> getArea(
            @LoginAppUser AppUser appUser
    ) {
        return areaService.getArea(appUser);
    }
}
