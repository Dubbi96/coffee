package com.coffee.atom.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class FileResponseDto {
    @Schema(description = "파일 URL")
    private String fileUrl;
    @Schema(description = "파일 저장 이름")
    private String fileName;

    public static FileResponseDto from(FileDto fileDto) {
        return FileResponseDto.builder()
                .fileUrl(fileDto.getUrl())
                .fileName(fileDto.getName())
                .build();
    }

}
