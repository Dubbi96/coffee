package com.coffee.atom.dto.appuser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
public class ViceAdminRequestDto {
    @Schema(description = "수정할 이름", example = "홍길동")
    private String username;

    @Schema(description = "수정할 사용자 ID", example = "hong123")
    private String userId;

    @Schema(description = "관리 지역 ID", example = "1")
    private Long areaId;

    @JsonIgnore
    @Schema(description = "신분증 파일 (선택)")
    private MultipartFile idCardFile;
}
