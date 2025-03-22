package com.coffee.atom.controller;

import com.coffee.atom.common.IgnoreResponseBinding;
import com.coffee.atom.config.security.LoginAppUser;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.file.FileEventLogRepository;
import com.coffee.atom.domain.file.FileEventLogType;
import com.coffee.atom.dto.file.FileDeleteRequestDto;
import com.coffee.atom.service.file.FileEventLogService;
import com.coffee.atom.util.GCSUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.isNull;

@RestController
@RequestMapping("/gcs")
@RequiredArgsConstructor
public class GCSController {
    private final GCSUtil gcsUtil;
    private final FileEventLogService fileEventLogService;

    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "단일 파일 업로드 API", description = "단일 파일 업로드")
    public String uploadFileToGCS(@LoginAppUser AppUser appUser,
                                  @RequestParam(value = "directory", required = false) String directory,
                                  @RequestPart(value = "file") MultipartFile file) {
        if (isNull(file)) throw new IllegalArgumentException("file is empty");
        try {
            return gcsUtil.uploadFileToGCS(directory, file, appUser);
        } catch (IOException e) {
            fileEventLogService.saveLog(appUser, FileEventLogType.UPLOAD, file, null, false);
            throw new IllegalStateException("파일 업로드 중 에러가 발생했습니다.");
        }
    }

    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "복수 파일 업로드 API", description = "복수 파일 업로드")
    public List<String> uploadFilesToGCS(@LoginAppUser AppUser appUser,
                                         @RequestParam(value = "directory", required = false) String directory,
                                         @RequestPart(value = "files") List<MultipartFile> files) {
        if (files.isEmpty()) throw new IllegalArgumentException("files are empty");
        try {
            return gcsUtil.uploadFilesToGCS(directory, files, appUser);
        } catch (IOException e) {
            fileEventLogService.saveLogs(appUser, FileEventLogType.UPLOAD, files, Collections.emptyList(), false);
            throw new IllegalStateException("파일 업로드 중 에러가 발생했습니다.");
        }
    }

    @DeleteMapping(value = "/files")
    @Operation(summary = "복수 파일 삭제 API", description = "복수 파일 삭제")
    public void deleteFilesFromGCS(@LoginAppUser AppUser appUser,
                                   @RequestBody FileDeleteRequestDto fileDeleteRequestDto) {
        if (isNull(fileDeleteRequestDto.getFileUrls()) || fileDeleteRequestDto.getFileUrls().isEmpty()) return;
        gcsUtil.deleteFileFromGCS(fileDeleteRequestDto.getFileUrls(), appUser);
    }

    @GetMapping("/download")
    @IgnoreResponseBinding
    public ResponseEntity<InputStreamResource> downloadFile(@LoginAppUser AppUser appUser,
                                                            @RequestParam String fileUrl) {
        InputStream inputStream = gcsUtil.downloadFileFromGCS(fileUrl, appUser);
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + getFileName(fileUrl) + "\"")
            .body(new InputStreamResource(inputStream));
    }

    private String getFileName(String fileUrl) {
        return URLDecoder.decode(fileUrl.substring(fileUrl.lastIndexOf("/") + 1), StandardCharsets.UTF_8);
    }

    @GetMapping("/image")
    @IgnoreResponseBinding
    public ResponseEntity<InputStreamResource> getImage(@LoginAppUser AppUser appUser,
                                                        @RequestParam String fileUrl) {
        InputStream imageStream = gcsUtil.downloadFileFromGCS(fileUrl, appUser);
        MediaType mediaType = gcsUtil.getMediaType(fileUrl);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new InputStreamResource(imageStream));
    }
}
