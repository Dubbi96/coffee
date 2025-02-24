package com.coffee.atom.config.error;

import static com.coffee.atom.common.ApiResponse.makeErrorResponse;

import com.coffee.atom.common.ApiResponse;
import com.coffee.atom.config.CodeValue;
import lombok.extern.slf4j.Slf4j;
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

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        return makeErrorResponse(e.getMessage(), CodeValue.BAD_REQUEST.getValue());
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
        log.error(e.getMessage(), e);
        return makeErrorResponse(e.getMessage(), CodeValue.INTERNAL_ERROR.getValue(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
