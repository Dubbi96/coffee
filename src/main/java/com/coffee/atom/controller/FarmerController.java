package com.coffee.atom.controller;

import com.coffee.atom.config.security.LoginAppUser;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.dto.FarmerResponseDto;
import com.coffee.atom.dto.FarmersResponseDto;
import com.coffee.atom.service.FarmerService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/farmer")
@RequiredArgsConstructor
public class FarmerController {
    private final FarmerService farmerService;

    @GetMapping
    @Operation(
        summary = "농부 목록 조회",
        description = "<b>역할(Role)에 따라 조회되는 농부 목록 조회 로직 변경</b><br>" +
                      "<b>ADMIN</b>: 모든 농부 목록 조회<br>" +
                      "<b>VICE_ADMIN_HEAD_OFFICER / VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER</b>: 자신의 Area 내 VillageHead에 속한 농부 목록 조회<br>" +
                      "<b>VILLAGE_HEAD</b>: 본인에게 직접 소속된 농부 목록 조회<br>"
    )
    public List<FarmersResponseDto> getFarmersWithVillageHeadAndSection(@LoginAppUser AppUser appUser){
        return farmerService.getFarmersWithVillageHeadAndSection(appUser);
    }

    @GetMapping("/{farmerId}")
    @Operation(
        summary = "단일 농부의 나무 수령 이력 조회",
        description = "<b>farmerId에 해당하는 농부의 정보를 조회</b><br>" +
                      "해당 농부가 받은 나무 품종, 수령일자, 수량 정보를 포함한 이력을 반환<br>" +
                      "승인된(transaction.isApproved = true) 데이터만 조회 <br>" +
                      "응답에는 농부 이름과 해당 섹션(section) 이름도 포함"
    )
    public FarmerResponseDto getFarmerDetail(@PathVariable("farmerId") Long farmerId){
        return farmerService.getFarmerTreeTransactions(farmerId);
    }

    //TODO: 1. 나무 수령 목록 조회 (Admin, 부 관리자, 면장) 각각 다른 권한으로 조회

}
