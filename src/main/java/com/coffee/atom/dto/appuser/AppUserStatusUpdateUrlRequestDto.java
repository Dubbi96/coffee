package com.coffee.atom.dto.appuser;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AppUserStatusUpdateUrlRequestDto {
    @Schema(description = "수정할 사용자명", example = "홍길동")
    private String username;

    @Schema(description = "수정할 비밀번호", example = "pw")
    private String password;

    @Schema(description = "신분증 URL (VICE_ADMIN 계열만 사용, 선택)")
    private String idCardUrl;
}


