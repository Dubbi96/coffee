package com.coffee.atom.service.file;

import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.file.FileEventLog;
import com.coffee.atom.domain.file.FileEventLogRepository;
import com.coffee.atom.domain.file.FileEventLogType;
import com.coffee.atom.dto.file.FileEventResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileEventLogService {

    private final FileEventLogRepository fileEventLogRepository;

    @Async
    @Transactional
    public void saveLog(AppUser user, FileEventLogType type, MultipartFile file, String savedUrl, boolean isSuccess) {
        FileEventLog log = FileEventLog.builder()
                .appUser(user)
                .type(type)
                .name(file.getOriginalFilename())
                .file(savedUrl)
                .size(String.valueOf(file.getSize()))
                .isSuccess(isSuccess)
                .number(1L)
                .build();
        fileEventLogRepository.save(log);
    }

    @Async
    @Transactional
    public void saveLogs(AppUser user, FileEventLogType type, List<MultipartFile> files, List<String> savedUrls, boolean isSuccess) {
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            String url = savedUrls.get(i);
            FileEventLog log = FileEventLog.builder()
                    .appUser(user)
                    .type(type)
                    .name(file.getOriginalFilename())
                    .file(url)
                    .size(String.valueOf(file.getSize()))
                    .isSuccess(isSuccess)
                    .number((long) files.size())
                    .build();
            fileEventLogRepository.save(log);
        }
    }

    @Async
    @Transactional
    public void saveDownloadLog(AppUser user, String fileUrl, boolean isSuccess) {
        FileEventLog log = FileEventLog.builder()
                .appUser(user)
                .type(FileEventLogType.DOWNLOAD)
                .file(fileUrl)
                .isSuccess(isSuccess)
                .number(1L)
                .build();
        fileEventLogRepository.save(log);
    }

    @Async
    @Transactional
    public void saveDeleteLogs(List<String> deletedFileUrls, AppUser appUser) {
        List<FileEventLog> logs = deletedFileUrls.stream()
            .map(url -> FileEventLog.builder()
                .appUser(appUser)
                .type(FileEventLogType.DELETE)
                .name(getOriginalFileNameFromUrl(url))
                .file(url)
                .isSuccess(true)
                .size(null)
                .build())
            .collect(Collectors.toList());

        fileEventLogRepository.saveAll(logs);
    }

    private String getOriginalFileNameFromUrl(String url) {
        String path = url.substring(url.lastIndexOf('/') + 1);
        int sepIndex = path.lastIndexOf("--");
        return sepIndex != -1 ? path.substring(0, sepIndex) + path.substring(path.lastIndexOf('.')) : path;
    }

    @Transactional(readOnly = true)
    public List<FileEventResponseDto> getLogs(AppUser appUser, @Nullable FileEventLogType type) {
        List<FileEventLog> logs = (type == null)
                ? fileEventLogRepository.findByAppUserOrderByCreatedAtDesc(appUser)
                : fileEventLogRepository.findByAppUserAndTypeOrderByCreatedAtDesc(appUser, type);

        return logs.stream()
                .map(log -> new FileEventResponseDto(
                        log.getFile(),
                        log.getName(),
                        log.getNumber(),
                        log.getCreatedAt(),
                        log.getType(),
                        log.getAppUser().getId(),
                        log.getSize(),
                        log.getIsSuccess()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FileEventResponseDto> getCurrentExistingFiles() {
        List<FileEventLog> logs = fileEventLogRepository.findAllByOrderByCreatedAtDesc();

        Map<String, FileEventLog> latestEventByFile = new LinkedHashMap<>();

        for (FileEventLog log : logs) {
            String fileUrl = log.getFile();
            if (!latestEventByFile.containsKey(fileUrl)) {
                latestEventByFile.put(fileUrl, log);
            }
        }

        return latestEventByFile.values().stream()
                .filter(log -> log.getType() != FileEventLogType.DELETE)
                .map(log -> new FileEventResponseDto(
                        log.getFile(),
                        log.getName(),
                        log.getNumber(),
                        log.getCreatedAt(),
                        log.getType(),
                        log.getAppUser() != null ? log.getAppUser().getId() : null,
                        log.getSize(),
                        log.getIsSuccess()
                ))
                .toList();
    }
}