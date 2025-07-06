package com.coffee.atom.controller;

import com.coffee.atom.common.ApiResponse;
import com.coffee.atom.dto.area.SectionDto;
import com.coffee.atom.dto.section.SectionRequestDto;
import com.coffee.atom.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/section")
@RequiredArgsConstructor
public class SectionController {
    private final SectionService sectionService;

    @PostMapping()
    @Operation(
        summary = "섹션 생성 1️⃣ 총 관리자",
        description = "<b>섹션 명, 섹션 위도, 경도로 신규 섹션 생성</b><br>" +
                      "신규 섹션 생성은 ADMIN 권한만 사용 가능<br>" +
                      "타 권한의 AppUser로 해당 서비스 호출 시 UNAUTHORIZED 메세지 반환"
    )
    public void saveSection(
            @RequestBody SectionRequestDto sectionRequestDto
    ) {
        sectionService.createSection(sectionRequestDto);
    }

    @DeleteMapping("/{sectionId}")
    @Operation(
        summary = "섹션 삭제 1️⃣ 총 관리자",
        description = "<b>섹션 명, 섹션 위도, 경도로 신규 섹션 생성</b><br>" +
                      "신규 섹션 생성은 ADMIN 권한만 사용 가능<br>" +
                      "타 권한의 AppUser로 해당 서비스 호출 시 UNAUTHORIZED 메세지 반환"
    )
    public void deleteSection(
            @PathVariable("sectionId") Long sectionId
    ) {
        sectionService.deleteSection(sectionId);
    }

    @GetMapping("/{sectionId}")
    @Operation(
        summary = "섹션 단건 조회",
        description = "<b>지정한 섹션 ID에 해당하는 섹션 정보를 조회</b><br>" +
                      "섹션명, 경도(longitude), 위도(latitude)를 포함한 정보를 반환합니다.<br>" +
                      "해당 ID가 존재하지 않을 경우 예외가 발생합니다."
    )
    public SectionDto getSectionById(
            @PathVariable("sectionId") Long sectionId
    ) {
        return sectionService.getSectionById(sectionId);
    }

}
