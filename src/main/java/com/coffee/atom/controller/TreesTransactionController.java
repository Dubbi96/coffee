package com.coffee.atom.controller;

import com.coffee.atom.config.security.LoginAppUser;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.dto.TreeTransactionResponseDto;
import com.coffee.atom.service.TreesTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tree-transactions")
public class TreesTransactionController {

    private final TreesTransactionService treeTransactionService;

    @GetMapping
    @Operation(summary = "나무 수령 목록 조회", description = "<b>로그인한 유저의 Role에 따라 나무 수령 목록을 조회</b><br>ADMIN: 전체<br>부관리자: Area기준<br>면장: Section기준")
    public List<TreeTransactionResponseDto> getTreeTransactions(@LoginAppUser AppUser appUser) {
        return treeTransactionService.getTreeTransactions(appUser);
    }
}
