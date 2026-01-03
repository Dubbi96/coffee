package com.coffee.atom.dto.appuser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ViceAdminUpdateUrlRequestDto {
    @Schema(description = "수정할 이름", example = "홍길동")
    private String username;

    @Schema(description = "수정할 사용자 ID", example = "hong123")
    private String userId;

    @Schema(description = "관리 지역 ID", example = "1")
    private Long areaId;

    @Schema(description = "신분증 URL (선택)")
    private String idCardUrl;
}


