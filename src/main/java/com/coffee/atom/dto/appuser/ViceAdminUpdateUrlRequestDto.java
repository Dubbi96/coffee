package com.coffee.atom.dto.appuser;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ViceAdminUpdateUrlRequestDto {
    @Schema(description = "수정할 이름", example = "홍길동")
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 1, max = 50, message = "사용자명은 1자 이상 50자 이하여야 합니다")
    private String username;

    @Schema(description = "수정할 사용자 ID", example = "hong123")
    @NotBlank(message = "사용자 ID는 필수입니다")
    @Size(min = 1, max = 50, message = "사용자 ID는 1자 이상 50자 이하여야 합니다")
    private String userId;

    @Schema(description = "관리 지역 ID", example = "1")
    @NotNull(message = "관리 지역 ID는 필수입니다")
    @Positive(message = "관리 지역 ID는 양수여야 합니다")
    private Long areaId;

    @Schema(description = "신분증 URL (선택)")
    @Size(max = 2048, message = "URL은 2048자 이하여야 합니다")
    private String idCardUrl;
}


