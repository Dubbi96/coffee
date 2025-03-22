package com.coffee.atom.dto.file;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDownloadResponseDto {
    private String fileName;
    private String contentType;
    private String base64Data;
}