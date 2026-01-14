package com.coffee.atom.dto.appuser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ADMIN 사용자 생성 요청 DTO
 * 서버 내부에서만 사용 (프로파일 제한)
 */
@Data
@Schema(description = "ADMIN 사용자 생성 요청")
public class CreateAdminRequestDto {
    
    @NotBlank(message = "사용자 ID는 필수입니다")
    @Size(min = 1, max = 50, message = "사용자 ID는 1자 이상 50자 이하여야 합니다")
    @Schema(description = "사용자 ID", example = "admin", required = true)
    private String userId;
    
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 1, max = 50, message = "사용자명은 1자 이상 50자 이하여야 합니다")
    @Schema(description = "사용자명", example = "admin", required = true)
    private String username;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 1, max = 255, message = "비밀번호는 1자 이상 255자 이하여야 합니다")
    @Schema(description = "비밀번호", example = "admin", required = true)
    private String password;
}
