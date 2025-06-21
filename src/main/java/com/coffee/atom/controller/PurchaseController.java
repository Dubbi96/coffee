package com.coffee.atom.controller;

import com.coffee.atom.common.ApiResponse;
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
    @Operation(summary = "수매 목록 조회", description = "<b>Role에 따라 수매 목록을 조회</b><br>ADMIN은 전체, VICE_ADMIN_HEAD_OFFICER는 본인 것만")
    public List<PurchaseResponseDto> getPurchases(@LoginAppUser AppUser appUser) {
        return purchaseService.getPurchaseList(appUser);
    }

}
