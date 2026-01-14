package com.coffee.atom.dto.appuser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AppUserStatusUpdateUrlRequestDto {
    @Schema(description = "수정할 사용자명", example = "홍길동")
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 1, max = 50, message = "사용자명은 1자 이상 50자 이하여야 합니다")
    private String username;

    @Schema(description = "수정할 비밀번호", example = "pw")
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 1, max = 255, message = "비밀번호는 1자 이상 255자 이하여야 합니다")
    private String password;

    @Schema(description = "신분증 URL (VICE_ADMIN 계열만 사용, 선택)")
    @Size(max = 2048, message = "URL은 2048자 이하여야 합니다")
    private String idCardUrl;
}


