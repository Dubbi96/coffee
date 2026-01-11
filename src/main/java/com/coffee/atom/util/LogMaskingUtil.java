package com.coffee.atom.util;

import java.util.regex.Pattern;

/**
 * 로깅 시 민감 정보를 마스킹하기 위한 유틸리티 클래스
 * 
 * 주의: 로그에 패스워드, 토큰, API 키 등 민감한 정보를 기록하지 않도록 주의하세요.
 */
public class LogMaskingUtil {
    
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("(?i)(password|pwd|pass)=([^,\\s]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(?i)(token|access-token|authorization|bearer)\\s*[:=]\\s*([^,\\s]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(?i)(apikey|api-key|secret|secret-key)\\s*[:=]\\s*([^,\\s]+)", Pattern.CASE_INSENSITIVE);
    
    private static final String MASKED_VALUE = "***MASKED***";
    
    /**
     * 로그 메시지에서 민감 정보를 마스킹합니다.
     * 
     * @param message 원본 로그 메시지
     * @return 마스킹된 로그 메시지
     */
    public static String maskSensitiveData(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        String masked = message;
        masked = PASSWORD_PATTERN.matcher(masked).replaceAll("$1=" + MASKED_VALUE);
        masked = TOKEN_PATTERN.matcher(masked).replaceAll("$1: " + MASKED_VALUE);
        masked = API_KEY_PATTERN.matcher(masked).replaceAll("$1: " + MASKED_VALUE);
        
        return masked;
    }
    
    /**
     * 패스워드를 마스킹합니다.
     */
    public static String maskPassword(String password) {
        return password == null ? null : MASKED_VALUE;
    }
    
    /**
     * 토큰을 마스킹합니다.
     */
    public static String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return token;
        }
        // 토큰의 일부만 표시 (디버깅용)
        if (token.length() > 10) {
            return token.substring(0, 10) + "..." + MASKED_VALUE;
        }
        return MASKED_VALUE;
    }
}

