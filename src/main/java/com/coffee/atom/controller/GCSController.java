package com.coffee.atom.controller;

import com.coffee.atom.common.IgnoreResponseBinding;
import com.coffee.atom.config.security.LoginAppUser;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
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
    @Operation(
            summary = "단일 파일 업로드",
            description = "<b>GCS 버킷에 단일 파일을 업로드</b><br>" +
                    "업로드한 파일은 UUID 기반 고유파일 명으로 저장하며,<br>" +
                    "업로드 성공 여부에 따라 <code>FileEventLog</code>에 로그 기록<br>" +
                    "업로드 경로는 선택적으로 지정 가능하며, 지정하지 않으면 루트 디렉토리에 저장"
    )
    public String uploadFileToGCS(@LoginAppUser AppUser appUser,
                                  @RequestParam(value = "directory", required = false) String directory,
                                  @RequestPart(value = "file") MultipartFile file) {
        if (isNull(file)) throw new CustomException(ErrorValue.FILE_EMPTY);
        try {
            return gcsUtil.uploadFileToGCS(directory, file, appUser);
        } catch (IOException e) {
            fileEventLogService.saveLog(appUser, FileEventLogType.UPLOAD, file, null, false);
            throw new CustomException(ErrorValue.UNKNOWN_ERROR);
        }
    }

    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
    summary = "복수 파일 업로드",
    description = "<b>GCS 버킷에 복수의 파일 업로드.</b><br>" +
            "각 파일은 UUID 기반 고유 파일명으로 저장,<br>" +
            "업로드 성공 여부에 따라 <code>FileEventLog</code>에 로그 기록<br>" +
            "업로드 경로는 선택적으로 지정 가능하며, 지정하지 않으면 루트 디렉토리에 저장"
    )
    public List<String> uploadFilesToGCS(@LoginAppUser AppUser appUser,
                                         @RequestParam(value = "directory", required = false) String directory,
                                         @RequestPart(value = "files") List<MultipartFile> files) {
        if (files.isEmpty()) throw new CustomException(ErrorValue.FILES_EMPTY);
        try {
            return gcsUtil.uploadFilesToGCS(directory, files, appUser);
        } catch (IOException e) {
            fileEventLogService.saveLogs(appUser, FileEventLogType.UPLOAD, files, Collections.emptyList(), false);
            throw new CustomException(ErrorValue.UNKNOWN_ERROR);
        }
    }

    @DeleteMapping(value = "/files")
    @Operation(
            summary = "복수 파일 삭제",
            description = "<b>GCS 버킷에서 복수 파일을 삭제</b><br>" +
                    "파일 URL 리스트를 전달하면 해당 파일들을 GCS에서 삭제하며,<br>" +
                    "삭제된 파일들은 <code>FileEventLog</code>에 <b>DELETE</b> 타입으로 로그로 기록"
    )
    public void deleteFilesFromGCS(@LoginAppUser AppUser appUser,
                                   @RequestBody FileDeleteRequestDto fileDeleteRequestDto) {
        if (isNull(fileDeleteRequestDto.getFileUrls()) || fileDeleteRequestDto.getFileUrls().isEmpty()) return;
        gcsUtil.deleteFileFromGCS(fileDeleteRequestDto.getFileUrls(), appUser);
    }

    @GetMapping("/download")
    @IgnoreResponseBinding
    @Operation(
            summary = "파일 다운로드",
            description = "<b>GCS 버킷에서 파일을 다운로드</b><br>" +
                    "요청된 파일 URL을 통해 GCS에서 파일을 스트림으로 읽어와 응답하며,<br>" +
                    "InputStreamResource로 응답하기 위해 ***ResponseEntity***를 예외적으로 사용<br>" +
                    "파일 다운로드 결과는 <code>FileEventLog</code>에 <b>DOWNLOAD</b> 타입으로 로그 기록<br>"
    )
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
    @Operation(
        summary = "이미지 미리보기",
        description = "<b>GCS 버킷에 저장된 이미지 파일을 스트림으로 반환</b><br>" +
                "파일 URL을 통해 이미지 스트림을 반환하며,<br>" +
                "InputStreamResource로 응답하기 위해 ***ResponseEntity***를 예외적으로 사용<br>" +
                "파일 확장자에 따라 <code>MediaType</code>을 설정해 응답<br>" +
                "이미지에 한정된 API이므로 ***PNG, JPEG, GIF*** 형식만 지원 가능"
    )
    public ResponseEntity<InputStreamResource> getImage(@LoginAppUser AppUser appUser,
                                                        @RequestParam String fileUrl) {
        InputStream imageStream = gcsUtil.downloadFileFromGCS(fileUrl, appUser);
        MediaType mediaType = gcsUtil.getMediaType(fileUrl);

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(new InputStreamResource(imageStream));
    }
}
