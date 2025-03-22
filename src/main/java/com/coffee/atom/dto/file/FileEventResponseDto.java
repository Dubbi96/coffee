package com.coffee.atom.dto.file;

import com.coffee.atom.domain.file.FileEventLogType;
import com.coffee.atom.service.file.FileEventLogService;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Builder
public class FileEventResponseDto {
    private String fileUrl;
    private String fileName;
    private Long number;
    private LocalDateTime createdAt;
    private FileEventLogType type;
    private Long appUserId;
    private String size;
    private Boolean status;

    public FileEventResponseDto(String fileUrl, String fileName, Long number, LocalDateTime createdAt, FileEventLogType type, Long appUserId, String size, Boolean status) {
        this.fileUrl = fileUrl;
        this.fileName = fileName;
        this.number = number;
        this.createdAt = createdAt;
        this.type = type;
        this.appUserId = appUserId;
        this.size = size;
        this.status = status;
    }
}
