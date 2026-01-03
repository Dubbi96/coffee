package com.coffee.atom.dto.appuser;

import com.coffee.atom.domain.appuser.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SignUpUrlRequestDto {
    @Schema(description = "사용자 ID", example = "hong123")
    private String userId;

    @Schema(description = "사용자명", example = "홍길동")
    private String username;

    @Schema(description = "비밀번호", example = "pw")
    private String password;

    @Schema(description = "역할", example = "VICE_ADMIN_HEAD_OFFICER")
    private Role role;

    @Schema(description = "관리 지역 ID (VICE_ADMIN 계열 필수)", example = "1")
    private Long areaId;

    @Schema(description = "배정 Section ID (VILLAGE_HEAD 필수)", example = "1")
    private Long sectionId;

    @Schema(description = "은행명 (VILLAGE_HEAD 선택)", example = "KB")
    private String bankName;

    @Schema(description = "계좌정보 (VILLAGE_HEAD 선택)", example = "123-45-6789")
    private String accountInfo;

    @Schema(description = "면장 신원확인 이미지 URL (VILLAGE_HEAD 선택) / (VICE_ADMIN 공통 선택)")
    private String identificationPhotoUrl;

    @Schema(description = "면장 계약서 URL (VILLAGE_HEAD 선택)")
    private String contractFileUrl;

    @Schema(description = "면장 통장사본 URL (VILLAGE_HEAD 선택)")
    private String bankbookPhotoUrl;
}


