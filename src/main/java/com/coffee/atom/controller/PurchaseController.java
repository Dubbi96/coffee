package com.coffee.atom.controller;

import com.coffee.atom.config.security.LoginAppUser;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.dto.PurchaseResponseDto;
import com.coffee.atom.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/purchase")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping
    @Operation(
        summary = "수매 목록 조회", 
        description = "<b>역할(Role)에 따라 수매 목록을 조회</b><br>" +
                      "<b>ADMIN</b>: 승인된 전체 수매 목록 조회<br>" +
                      "<b>VICE_ADMIN_HEAD_OFFICER / VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER</b>: 본인이 관리자(manager)로 지정된 수매 목록만 조회<br>" +
                      "<b>VILLAGE_HEAD</b>: 본인과 1:1 관계인 수매 목록만 조회 (정책 2.2)<br>" +
                      "<b>⚠️ 정책 변경사항:</b><br>" +
                      "- Purchase는 면장과 1:1 관계로 기록됨<br>" +
                      "- 각 면장당 하나의 Purchase 기록만 존재<br>" +
                      "정렬: 구매일자(purchaseDate) 기준 내림차순"
    )
    public List<PurchaseResponseDto> getPurchases(@LoginAppUser AppUser appUser) {
        return purchaseService.getPurchaseList(appUser);
    }

}
