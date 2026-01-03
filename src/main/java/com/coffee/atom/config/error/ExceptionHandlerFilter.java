package com.coffee.atom.config.error;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.coffee.atom.common.ApiResponse;
import com.coffee.atom.config.CodeValue;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings({"rawtypes"})
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;
    
    @Value("${debug.logging.enabled:false}")
    private boolean debugLoggingEnabled;
    
    @Value("${debug.logging.path:.cursor/debug.log}")
    private String debugLogPath;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // #region agent log
        if (debugLoggingEnabled) {
            try {
                String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\",\"location\":\"ExceptionHandlerFilter.doFilterInternal:29\",\"message\":\"Exception filter entry\",\"data\":{\"method\":\"%s\",\"uri\":\"%s\"},\"timestamp\":%d}%n", 
                    request.getMethod(), request.getRequestURI(), System.currentTimeMillis());
                Files.write(Paths.get(debugLogPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {}
        }
        // #endregion
        try {
            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            // #region agent log
            if (debugLoggingEnabled) {
                try {
                    String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\",\"location\":\"ExceptionHandlerFilter.doFilterInternal:37\",\"message\":\"CustomException caught\",\"data\":{\"message\":\"%s\",\"code\":\"%s\"},\"timestamp\":%d}%n", 
                        e.getMessage(), e.getCode() != null ? e.getCode().getValue() : "null", System.currentTimeMillis());
                    Files.write(Paths.get(debugLogPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (Exception ex) {}
            }
            // #endregion
            setErrorResponse(HttpStatus.UNAUTHORIZED, response, new ApiResponse(e.getMessage(), e.getCode().getValue()));
        } catch (RuntimeException e) {
            // #region agent log
            if (debugLoggingEnabled) {
                try {
                    String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"E\",\"location\":\"ExceptionHandlerFilter.doFilterInternal:45\",\"message\":\"RuntimeException caught\",\"data\":{\"error\":\"%s\"},\"timestamp\":%d}%n", 
                        e.getClass().getSimpleName() + ": " + e.getMessage(), System.currentTimeMillis());
                    Files.write(Paths.get(debugLogPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (Exception ex) {}
            }
            // #endregion
            log.error("Spring Security Filter에서 에러가 발생하였습니다. (msg : " + e.getMessage() + ")", e);
            setErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, response, new ApiResponse(ErrorValue.UNKNOWN_ERROR.toString(),
                    CodeValue.INTERNAL_ERROR.getValue()));
        }

    }

    private void setErrorResponse(HttpStatus status, HttpServletResponse response, ApiResponse body) {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        try {
            String json = objectMapper.writeValueAsString(body);
            PrintWriter writer = response.getWriter();
            writer.write(json);
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
