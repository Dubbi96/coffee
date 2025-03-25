package com.coffee.atom.config.error;

import lombok.Getter;

@Getter
public enum ErrorValue {
    ACCESS_DENIED("허용되지 않은 접근입니다."),
    UNAUTHORIZED("인증되지 않은 사용자입니다."),
    UNAUTHORIZED_SERVICE("권한 외 요청입니다."),
    UNKNOWN_ERROR("알 수 없는 에러입니다."),
    TOKEN_NOT_FOUND("토큰이 존재하지 않습니다."),
    TOKEN_EXPIRED("토큰이 만료되었습니다."),
    ACCOUNT_NOT_FOUND("존재하지 않는 계정입니다."),
    AREA_NOT_FOUND("존재하지 않는 지역입니다."),
    FARMER_NOT_FOUND("해당 농부는 존재하지 않습니다."),
    SUBJECT_NOT_FOUND("존재하지 않는 종목입니다."),
    EMAIL_ALREADY_EXISTS("이미 존재하는 이메일입니다."),
    SENSOR_NOT_FOUND("존재하지 않는 센서 입니다."),

    USER_KEY_ALREADY_EXISTS("이미 존재하는 외부 유저입니다."),
    NICKNAME_ALREADY_EXISTS("이미 존재하는 닉네임입니다."),

    WITHDRAWAL_EMAIL("탈퇴한 이메일입니다."),
    WITHDRAWAL_USER_KEY("탈퇴한 유저 키입니다."),
    EMAIL_CODE_EXPIRED("인증코드가 만료되었습니다."),
    WRONG_ACCOUNT("올바르지 않은 비밀번호 및 아이디입니다."),

    PREMIUM_ALREADY("이미 프리미엄 회원입니다."),
    PROMOTION_ALREADY("이미 프로모션이 적용된 회원입니다."),
    PASSWORD_MISSING("비밀번호가 필요합니다."),
    WRONG_PHONE("잘못된 양식의 전화번호입니다."),

    WRONG_EMAIL("잘못된 양식의 이메일입니다."),

    USER_COUPON_NOT_FOUND("존재하지 않는 유저의 쿠폰입니다."),
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
