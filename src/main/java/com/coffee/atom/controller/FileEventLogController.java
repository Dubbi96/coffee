package com.coffee.atom.controller;

import com.coffee.atom.common.ApiResponse;
import com.coffee.atom.config.security.LoginAppUser;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.file.FileEventLogType;
import com.coffee.atom.dto.file.FileEventResponseDto;
import com.coffee.atom.service.file.FileEventLogService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/file-event")
@RequiredArgsConstructor
public class FileEventLogController {
    private final FileEventLogService fileEventLogService;

    @GetMapping("/logs")
    @Operation(summary = "파일 이벤트 로그 조회", description = "<b>파일 업로드/다운로드/삭제 로그 조회</b><br>타입 없이 전체 조회도 가능")
    public List<FileEventResponseDto> getFileLogs(
            @LoginAppUser AppUser appUser,
            @RequestParam(value = "type", required = false) FileEventLogType type
    ) {
        return fileEventLogService.getLogs(appUser, type);
    }

    @GetMapping("/existing-files")
    @Operation(summary = "존재하는 파일 조회", description = "<b>현재 버킷에 존재하는 파일만 조회</b><br>로그를 기반으로 버킷에 존재하는 파일만 조회")
    public List<FileEventResponseDto> getFileLogs(@LoginAppUser AppUser appUser) {
        return fileEventLogService.getCurrentExistingFiles(appUser);
    }
}
