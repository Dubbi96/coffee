package com.coffee.atom.config.error;

import com.coffee.atom.config.CodeValue;
import lombok.Getter;

@Getter
public class CustomException extends RuntimeException{

    private final String message;

    private final CodeValue code;

    private final Object data;

    /**
     * ErrorValue를 사용하는 생성자 (권장)
     */
    public CustomException(ErrorValue errorValue) {
        super(errorValue.getMessage());
        this.message = errorValue.getMessage();
        this.code = determineCode(errorValue);
        this.data = null;
    }

    /**
     * ErrorValue와 CodeValue를 함께 사용하는 생성자
     */
    public CustomException(ErrorValue errorValue, CodeValue code) {
        super(errorValue.getMessage());
        this.message = errorValue.getMessage();
        this.code = code;
        this.data = null;
    }

    /**
     * 메시지만 사용하는 생성자 (하위 호환성)
     * @deprecated ErrorValue를 사용하는 생성자를 권장합니다.
     */
    @Deprecated
    public CustomException(String message) {
        super(message);
        this.message = message;
        if (message.equals(ErrorValue.ACCOUNT_NOT_FOUND.getMessage())) {
            this.code = CodeValue.NO_USER_FROM_TOKEN;
        } else {
            this.code = CodeValue.FAILURE_MODAL;
        }
        this.data = null;
    }

    public CustomException(String message, CodeValue code) {
        super(message);
        this.message = message;
        this.code = code;
        this.data = null;
    }

    public CustomException(String message, CodeValue code, Object data) {
        super(message);
        this.message = message;
        this.code = code;
        this.data = data;
    }

    /**
     * ErrorValue에 따라 적절한 CodeValue를 결정
     */
    private CodeValue determineCode(ErrorValue errorValue) {
        return switch (errorValue) {
            case ACCOUNT_NOT_FOUND, APP_USER_NOT_FOUND -> CodeValue.NO_USER_FROM_TOKEN;
            case UNAUTHORIZED, UNAUTHORIZED_SERVICE, ACCESS_DENIED -> CodeValue.ACCESS_DENIED;
            case NICKNAME_ALREADY_EXISTS -> CodeValue.ALREADY_APPLICANT_EXISTS;
            default -> CodeValue.FAILURE_MODAL;
        };
    }
}
