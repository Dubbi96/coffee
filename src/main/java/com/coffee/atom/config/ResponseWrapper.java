package com.coffee.atom.config;

import com.coffee.atom.common.ApiResponse;
import com.coffee.atom.common.IgnoreResponseBinding;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice(
        basePackages = "com.coffee.atom.controller"
)
public class ResponseWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true; // 항상 처리
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response
    ) {
        // IgnoreResponseBinding이 붙은 경우 포장하지 않음
        if (returnType.getDeclaringClass().isAnnotationPresent(IgnoreResponseBinding.class)
            || returnType.hasMethodAnnotation(IgnoreResponseBinding.class)) {
            return body;
        }

        // 이미 ApiResponse로 감싼 경우 중복 방지
        if (body instanceof ApiResponse) {
            return body;
        }

        return ApiResponse.builder()
                .message("성공")
                .code(CodeValue.SUCCESS.getValue())
                .response(body)
                .build();
    }
}
