package com.coffee.atom.config.error;

import static com.coffee.atom.common.ApiResponse.makeErrorResponse;

import com.coffee.atom.common.ApiResponse;
import com.coffee.atom.config.CodeValue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@SuppressWarnings({"rawtypes"})
public class GlobalExceptionHandler {
    
    @Value("${spring.profiles.active:local}")
    private String activeProfile;
    
    private boolean isProduction() {
        return "prod".equals(activeProfile);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = isProduction() ? "입력 데이터가 유효하지 않습니다." : e.getMessage();
        log.error("입력 검증 오류: {}", e.getMessage(), e);
        return makeErrorResponse(message, CodeValue.BAD_REQUEST.getValue());
    }

    @ExceptionHandler(SessionAuthenticationException.class)
    public ResponseEntity<ApiResponse> handleAuthenticationException(SessionAuthenticationException e) {
        return makeErrorResponse(ErrorValue.UNAUTHORIZED.toString(), CodeValue.NO_TOKEN_IN_REQUEST.getValue(), HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse> handleCustomException(CustomException e) {
        return makeErrorResponse(e.getMessage(), e.getCode().getValue(), e.getData());
    }

    @ExceptionHandler(FileFailureException.class)
    public ResponseEntity<ApiResponse> handleFileFailureException(FileFailureException e) {
        return makeErrorResponse(e.getMessage(), CodeValue.INTERNAL_ERROR.getValue(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse> handleCustomException(RuntimeException e) {
        String message = isProduction() ? "서버 오류가 발생했습니다." : e.getMessage();
        log.error("런타임 오류 발생: {}", e.getMessage(), e);
        return makeErrorResponse(message, CodeValue.INTERNAL_ERROR.getValue(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        log.warn("무결성 제약 위반 발생: {}", e.getMessage(), e);
        String message = "삭제할 수 없습니다. 해당 항목이 다른 데이터에서 참조되고 있습니다.";
        return makeErrorResponse(message, CodeValue.DATA_INTEGRITY_VIOLATION.getValue(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        String message = isProduction() ? "잘못된 요청입니다." : e.getMessage();
        log.warn("잘못된 인자 예외 발생: {}", e.getMessage(), e);
        return makeErrorResponse(message, CodeValue.BAD_REQUEST.getValue(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse> handleIllegalStateException(IllegalStateException e) {
        String message = isProduction() ? "잘못된 요청입니다." : e.getMessage();
        log.warn("잘못된 상태 예외 발생: {}", e.getMessage(), e);
        return makeErrorResponse(message, CodeValue.BAD_REQUEST.getValue(), HttpStatus.BAD_REQUEST);
    }
}
