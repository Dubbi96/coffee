package com.coffee.atom.dto.appuser;

import com.coffee.atom.domain.appuser.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignUpUrlRequestDto {
    @Schema(description = "사용자 ID", example = "hong123")
    @NotBlank(message = "사용자 ID는 필수입니다")
    @Size(min = 1, max = 50, message = "사용자 ID는 1자 이상 50자 이하여야 합니다")
    private String userId;

    @Schema(description = "사용자명", example = "홍길동")
    @NotBlank(message = "사용자명은 필수입니다")
    @Size(min = 1, max = 50, message = "사용자명은 1자 이상 50자 이하여야 합니다")
    private String username;

    @Schema(description = "비밀번호", example = "pw")
    @NotBlank(message = "비밀번호는 필수입니다")
    @Size(min = 1, max = 255, message = "비밀번호는 1자 이상 255자 이하여야 합니다")
    private String password;

    @Schema(description = "역할", example = "VICE_ADMIN_HEAD_OFFICER")
    @NotNull(message = "역할은 필수입니다")
    private Role role;

    @Schema(description = "관리 지역 ID (VICE_ADMIN 계열 필수)", example = "1")
    private Long areaId;

    @Schema(description = "배정 Section ID (VILLAGE_HEAD 필수)", example = "1")
    private Long sectionId;

    @Schema(description = "은행명 (VILLAGE_HEAD 선택)", example = "KB")
    @Size(max = 255, message = "은행명은 255자 이하여야 합니다")
    private String bankName;

    @Schema(description = "계좌정보 (VILLAGE_HEAD 선택)", example = "123-45-6789")
    @Size(max = 255, message = "계좌정보는 255자 이하여야 합니다")
    private String accountInfo;

    @Schema(description = "면장 신원확인 이미지 URL (VILLAGE_HEAD 선택) / (VICE_ADMIN 공통 선택)")
    @Size(max = 2048, message = "URL은 2048자 이하여야 합니다")
    private String identificationPhotoUrl;

    @Schema(description = "면장 계약서 URL (VILLAGE_HEAD 선택)")
    @Size(max = 2048, message = "URL은 2048자 이하여야 합니다")
    private String contractFileUrl;

    @Schema(description = "면장 통장사본 URL (VILLAGE_HEAD 선택)")
    @Size(max = 2048, message = "URL은 2048자 이하여야 합니다")
    private String bankbookPhotoUrl;
}


