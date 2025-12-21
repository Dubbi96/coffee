package com.coffee.atom.config.error;

import lombok.Getter;

@Getter
public enum ErrorValue {
    // 인증/권한 관련
    ACCESS_DENIED("허용되지 않은 접근입니다."),
    UNAUTHORIZED("인증되지 않은 사용자입니다."),
    UNAUTHORIZED_SERVICE("권한 외 요청입니다."),
    UNKNOWN_ERROR("알 수 없는 에러입니다."),
    TOKEN_NOT_FOUND("토큰이 존재하지 않습니다."),
    TOKEN_EXPIRED("토큰이 만료되었습니다."),
    
    // 리소스 조회 관련
    ACCOUNT_NOT_FOUND("존재하지 않는 계정입니다."),
    AREA_NOT_FOUND("존재하지 않는 지역입니다."),
    FARMER_NOT_FOUND("해당 농부는 존재하지 않습니다."),
    SECTION_NOT_FOUND("해당 섹션은 존재하지 않습니다."),
    SUBJECT_NOT_FOUND("존재하지 않는 요청입니다."),
    PURCHASE_NOT_FOUND("존재하지 않는 구매 이력입니다."),
    VILLAGE_HEAD_NOT_FOUND("존재하지 않는 면장입니다."),
    VICE_ADMIN_NOT_FOUND("존재하지 않는 부 관리자입니다."),
    APP_USER_NOT_FOUND("존재하지 않는 사용자입니다."),
    
    // 승인 상태 관련
    SECTION_NOT_APPROVED("승인되지 않은 섹션입니다."),
    VILLAGE_HEAD_NOT_APPROVED("승인된 면장이 아닙니다."),
    
    // 계정 생성/수정 관련
    USER_KEY_ALREADY_EXISTS("이미 존재하는 외부 유저입니다."),
    NICKNAME_ALREADY_EXISTS("이미 존재하는 닉네임입니다."),
    USER_ID_ALREADY_EXISTS("이미 존재하는 사용자 ID입니다."),
    USERNAME_ALREADY_EXISTS("이미 존재하는 사용자명입니다."),
    USER_ID_REQUIRED("사용자 ID는 필수입니다."),
    USERNAME_REQUIRED("사용자명은 필수입니다."),
    PASSWORD_REQUIRED("비밀번호는 필수입니다."),
    SECTION_ID_REQUIRED("섹션 ID는 필수입니다."),
    USER_ID_TOO_LONG("사용자 ID 길이 제한(최대 50자)을 초과했습니다."),
    USERNAME_TOO_LONG("사용자명 길이 제한(최대 50자)을 초과했습니다."),
    BANK_NAME_TOO_LONG("은행명 길이 제한(최대 255자)을 초과했습니다."),
    ACCOUNT_INFO_TOO_LONG("계좌 정보 길이 제한(최대 255자)을 초과했습니다."),
    VILLAGE_HEAD_ID_REQUIRED("면장 ID는 필수입니다."),
    ADMIN_CREATION_NOT_ALLOWED("ADMIN 권한으로 계정을 생성할 수 없습니다."),
    VICE_ADMIN_ALREADY_EXISTS_IN_AREA("해당 지역에는 이미 해당 권한의 부관리자가 할당되어 있습니다."),
    VICE_ADMIN_INFO_NOT_FOUND("부 관리자 정보가 존재하지 않습니다."),
    VILLAGE_HEAD_DETAIL_NOT_FOUND("면장 세부정보를 찾을 수 없습니다."),
    
    // 지역/섹션 관련
    AREA_SECTION_MISMATCH("본인이 배정된 지역의 섹션으로만 면장을 생성할 수 있습니다."),
    VILLAGE_HEAD_AREA_MISMATCH("본인이 배정된 지역의 면장만 선택할 수 있습니다."),
    VILLAGE_HEAD_SECTION_MISMATCH("본인이 배정된 지역의 Section에만 면장을 배정할 수 있습니다."),
    FARMER_AREA_MISMATCH("본인이 배정된 지역의 면장 하위에만 농부를 생성할 수 있습니다."),
    
    // 역할 관련
    ROLE_NOT_ALLOWED_FARMER_LIST("해당 역할은 농부 목록을 조회할 수 없습니다."),
    ROLE_NOT_ALLOWED_APPROVAL_LIST("해당 권한으로 요청 목록을 조회할 수 없습니다."),
    
    // 파일 관련
    FILE_EMPTY("파일이 비어있습니다."),
    FILES_EMPTY("파일 목록이 비어있습니다."),
    FILE_NAME_INVALID("파일명이 유효하지 않습니다."),
    GCS_URL_INVALID("올바르지 않은 GCS URL입니다."),
    
    // 처리 관련
    JSON_PROCESSING_ERROR("요청 데이터를 파싱할 수 없습니다."),
    JWT_PARSING_ERROR("JWT 토큰 파싱 중 에러가 발생했습니다."),
    UNSUPPORTED_OPERATION("지원하지 않는 작업입니다."),
    WRONG_PASSWORD("올바르지 않은 아이디 및 비밀번호입니다."),
    VILLAGE_HEAD_UPDATE_AREA_MISMATCH("본인이 배정된 지역의 면장만 수정할 수 있습니다."),
    VILLAGE_HEAD_SECTION_ASSIGN_MISMATCH("본인이 배정된 지역의 섹션으로만 배정할 수 있습니다."),
    ID_CARD_UPLOAD_FAILED("ID 카드 업로드 실패"),
    
    // 기타 (기존)
    WITHDRAWAL_EMAIL("탈퇴한 이메일입니다."),
    WITHDRAWAL_USER_KEY("탈퇴한 유저 키입니다."),
    EMAIL_CODE_EXPIRED("인증코드가 만료되었습니다."),
    WRONG_ACCOUNT("올바르지 않은 비밀번호 및 아이디입니다."),
    PREMIUM_ALREADY("이미 프리미엄 회원입니다."),
    PROMOTION_ALREADY("이미 프로모션이 적용된 회원입니다."),
    PASSWORD_MISSING("비밀번호가 필요합니다."),
    WRONG_PHONE("잘못된 양식의 전화번호입니다."),
    WRONG_EMAIL("잘못된 양식의 이메일입니다."),
    RETRY_WITH_KAKAO("카카오 소셜로그인을 이용해주세요."),
    RETRY_WITH_APPLE("애플 소셜로그인을 이용해주세요."),
    WRONG_EMAIL_ADDRESS("잘못된 이메일 주소입니다.");

    private final String message;

    ErrorValue(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return this.message;
    }
}
