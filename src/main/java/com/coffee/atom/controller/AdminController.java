package com.coffee.atom.controller;

import com.coffee.atom.config.security.LoginAppUser;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.dto.approval.ApprovalTreesTransactionRequestDto;
import com.coffee.atom.service.TreesTransactionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final TreesTransactionService treesTransactionService;

    @PostMapping(value = "/trees-transaction")
    @Operation(
        summary = "나무 수령 등록 1️⃣총 관리자 ",
        description = "<b>나무 수령 정보 생성</b>"
    )
    public void requestApprovalToCreateTreesTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수령할 나무 정보<br>" +
                            "- <b>quantity</b>: 수량<br>" +
                            "- <b>receivedDate</b>: 수령 일자<br>" +
                            "- <b>species</b>: 나무 종<br>" +
                            "- <b>farmerId</b>: 수령 농부 ID",
                    required = true
            )
            @RequestBody ApprovalTreesTransactionRequestDto approvalTreesTransactionRequestDto
    ){
        treesTransactionService.createTreesTransaction(approvalTreesTransactionRequestDto);
    }
}
