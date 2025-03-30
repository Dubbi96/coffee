package com.coffee.atom.controller;

import com.coffee.atom.dto.section.SectionRequestDto;
import com.coffee.atom.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public void saveArea(
            @RequestBody SectionRequestDto sectionRequestDto
    ) {
        sectionService.createSection(sectionRequestDto);
    }
}
