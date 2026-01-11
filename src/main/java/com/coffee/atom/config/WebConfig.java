package com.coffee.atom.config;

import com.fasterxml.jackson.core.json.JsonWriteFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ObjectMapper objectMapper;

    @Value("${cors.allowed-origins:*}")
    private List<String> allowedOrigins;

    @Value("${cors.allowed-methods:GET,POST,PUT,PATCH,DELETE,OPTIONS,HEAD}")
    private List<String> allowedMethods;

    @Value("${cors.allowed-headers:*}")
    private List<String> allowedHeaders;

    @Value("${cors.max-age:3600}")
    private long maxAge;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        var registration = registry.addMapping("/**");
        
        // 허용된 origin 설정
        if (allowedOrigins.size() == 1 && allowedOrigins.get(0).equals("*")) {
            // 로컬 개발 환경: 모든 origin 허용
            registration.allowedOriginPatterns("*");
        } else {
            // 프로덕션 환경: 특정 origin만 허용
            registration.allowedOrigins(allowedOrigins.toArray(new String[0]));
        }
        
        // 허용된 헤더 설정
        if (allowedHeaders.size() == 1 && allowedHeaders.get(0).equals("*")) {
            registration.allowedHeaders("*");
        } else {
            registration.allowedHeaders(allowedHeaders.toArray(new String[0]));
        }
        
        // 허용된 HTTP 메서드 설정
        registration.allowedMethods(allowedMethods.toArray(new String[0]));
        registration.maxAge(maxAge);
        registration.allowCredentials(true);
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        objectMapper.getFactory().configure(JsonWriteFeature.ESCAPE_NON_ASCII.mappedFeature(), true);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        converters.add(1, new MappingJackson2HttpMessageConverter(objectMapper));
    }
}
