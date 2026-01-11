package com.coffee.atom.dto.appuser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignInRequestDto {
    @NotBlank(message = "사용자 ID는 필수입니다")
    @Size(min = 1, max = 50, message = "사용자 ID는 1자 이상 50자 이하여야 합니다")
    @Schema(example = "admin")
    private String userId;
    
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 1, max = 255, message = "비밀번호는 1자 이상 255자 이하여야 합니다")
    @Schema(example = "admin")
    private String password;
}
