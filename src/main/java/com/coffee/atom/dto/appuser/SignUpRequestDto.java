package com.coffee.atom.dto.appuser;

import com.coffee.atom.domain.appuser.Role;
import lombok.Data;

//TODO: RequestDto 수정 (화면 상 내 계정 등록) - 무조건 총 관리자만 사용 가능하며, 면장 부 관리자 생성 용으로 사용 됨. 면장의 경우 지역, 섹션 매핑, 부 관리자의 경우 지역 지역 매핑
@Data
public class SignUpRequestDto {
    private String userId;
    private String username;
    private String password;
    private Role role;
}