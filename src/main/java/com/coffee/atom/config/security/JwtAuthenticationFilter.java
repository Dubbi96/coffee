package com.coffee.atom.config.security;

import com.coffee.atom.config.Value;
import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.domain.appuser.AppUser;
import com.coffee.atom.domain.appuser.AppUserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

@Slf4j
@RequiredArgsConstructor
@Component
/*
    Request가 Controller까지 도착하기 전, 필터에서 헤더를 체크하여 access token이 있다면 검증을 진행한다.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final AppUserRepository appUserRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // #region agent log
        try {
            String logPath = "/Users/gangjong-won/Dubbi/Coffee-maven/.cursor/debug.log";
            String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\",\"location\":\"JwtAuthenticationFilter.doFilterInternal:38\",\"message\":\"Filter entry\",\"data\":{\"method\":\"%s\",\"uri\":\"%s\",\"headerNames\":\"%s\"},\"timestamp\":%d}%n", 
                request.getMethod(), request.getRequestURI(), java.util.Collections.list(request.getHeaderNames()).toString(), System.currentTimeMillis());
            Files.write(Paths.get(logPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {}
        // #endregion
        if (nonNull(SecurityContextHolder.getContext().getAuthentication())) {
            log.debug("SecurityContextHolder는 이미 authentication 객체를 가지고 있습니다.: '{}'",
                    SecurityContextHolder.getContext().getAuthentication());
            // #region agent log
            try {
                String logPath = "/Users/gangjong-won/Dubbi/Coffee-maven/.cursor/debug.log";
                String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"JwtAuthenticationFilter.doFilterInternal:42\",\"message\":\"Already authenticated\",\"data\":{\"auth\":\"%s\"},\"timestamp\":%d}%n", 
                    SecurityContextHolder.getContext().getAuthentication().getClass().getSimpleName(), System.currentTimeMillis());
                Files.write(Paths.get(logPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {}
            // #endregion
            chain.doFilter(request, response);
            return;
        }
        String jwtToken = request.getHeader(Value.ACCESS_TOKEN_HEADER_KEY);
        // #region agent log
        try {
            String logPath = "/Users/gangjong-won/Dubbi/Coffee-maven/.cursor/debug.log";
            String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\",\"location\":\"JwtAuthenticationFilter.doFilterInternal:50\",\"message\":\"Token header check\",\"data\":{\"headerKey\":\"%s\",\"tokenPresent\":%s,\"tokenPrefix\":\"%s\"},\"timestamp\":%d}%n", 
                Value.ACCESS_TOKEN_HEADER_KEY, jwtToken != null, jwtToken != null && jwtToken.length() > 20 ? jwtToken.substring(0, 20) + "..." : "null", System.currentTimeMillis());
            Files.write(Paths.get(logPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {}
        // #endregion
        if (isNull(jwtToken)) {
            // #region agent log
            try {
                String logPath = "/Users/gangjong-won/Dubbi/Coffee-maven/.cursor/debug.log";
                String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"A\",\"location\":\"JwtAuthenticationFilter.doFilterInternal:54\",\"message\":\"No token found\",\"data\":{},\"timestamp\":%d}%n", 
                    System.currentTimeMillis());
                Files.write(Paths.get(logPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {}
            // #endregion
            chain.doFilter(request, response);
            return;
        }
        Claims claims = null;
        try {
            claims = jwtProvider.verifyToken(jwtToken);
            // #region agent log
            try {
                String logPath = "/Users/gangjong-won/Dubbi/Coffee-maven/.cursor/debug.log";
                String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\",\"location\":\"JwtAuthenticationFilter.doFilterInternal:65\",\"message\":\"Token verified\",\"data\":{\"appUserId\":\"%s\"},\"timestamp\":%d}%n", 
                    claims.get("appUserId"), System.currentTimeMillis());
                Files.write(Paths.get(logPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception e) {}
            // #endregion
        } catch (Exception e) {
            // #region agent log
            try {
                String logPath = "/Users/gangjong-won/Dubbi/Coffee-maven/.cursor/debug.log";
                String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"B\",\"location\":\"JwtAuthenticationFilter.doFilterInternal:72\",\"message\":\"Token verification failed\",\"data\":{\"error\":\"%s\"},\"timestamp\":%d}%n", 
                    e.getClass().getSimpleName() + ": " + e.getMessage(), System.currentTimeMillis());
                Files.write(Paths.get(logPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (Exception ex) {}
            // #endregion
            throw e;
        }
        AuthenticationToken authenticationToken = authenticate(claims);
        // #region agent log
        try {
            String logPath = "/Users/gangjong-won/Dubbi/Coffee-maven/.cursor/debug.log";
            String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"JwtAuthenticationFilter.doFilterInternal:81\",\"message\":\"Authentication created\",\"data\":{\"authenticated\":%s,\"authorities\":\"%s\"},\"timestamp\":%d}%n", 
                authenticationToken.isAuthenticated(), authenticationToken.getAuthorities().toString(), System.currentTimeMillis());
            Files.write(Paths.get(logPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {}
        // #endregion
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        // #region agent log
        try {
            String logPath = "/Users/gangjong-won/Dubbi/Coffee-maven/.cursor/debug.log";
            String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"JwtAuthenticationFilter.doFilterInternal:88\",\"message\":\"SecurityContext set\",\"data\":{},\"timestamp\":%d}%n", 
                System.currentTimeMillis());
            Files.write(Paths.get(logPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {}
        // #endregion
        chain.doFilter(request, response);
    }
    private AuthenticationToken authenticate(Claims claims) {
        if (claims.get("appUserId") != null) {
            return authenticateAppUser(Long.parseLong(claims.get("appUserId").toString()));
        }
        throw new CustomException(ErrorValue.UNAUTHORIZED);
    }

    private AuthenticationToken authenticateAppUser(Long appUserId) {
        // #region agent log
        try {
            String logPath = "/Users/gangjong-won/Dubbi/Coffee-maven/.cursor/debug.log";
            String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"JwtAuthenticationFilter.authenticateAppUser:96\",\"message\":\"Looking up user\",\"data\":{\"appUserId\":%d},\"timestamp\":%d}%n", 
                appUserId, System.currentTimeMillis());
            Files.write(Paths.get(logPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {}
        // #endregion
        AppUser appUser = appUserRepository.findById(appUserId)
                .orElseThrow(() -> {
                    // #region agent log
                    try {
                        String logPath = "/Users/gangjong-won/Dubbi/Coffee-maven/.cursor/debug.log";
                        String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"JwtAuthenticationFilter.authenticateAppUser:100\",\"message\":\"User not found\",\"data\":{\"appUserId\":%d},\"timestamp\":%d}%n", 
                            appUserId, System.currentTimeMillis());
                        Files.write(Paths.get(logPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                    } catch (Exception e) {}
                    // #endregion
                    return new CustomException(ErrorValue.ACCOUNT_NOT_FOUND);
                });
        // #region agent log
        try {
            String logPath = "/Users/gangjong-won/Dubbi/Coffee-maven/.cursor/debug.log";
            String logEntry = String.format("{\"sessionId\":\"debug-session\",\"runId\":\"run1\",\"hypothesisId\":\"C\",\"location\":\"JwtAuthenticationFilter.authenticateAppUser:109\",\"message\":\"User found\",\"data\":{\"appUserId\":%d,\"role\":\"%s\"},\"timestamp\":%d}%n", 
                appUser.getId(), appUser.getRole() != null ? appUser.getRole().name() : "null", System.currentTimeMillis());
            Files.write(Paths.get(logPath), logEntry.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {}
        // #endregion
        TokenPrincipal principal = new TokenPrincipal(appUser);
        SimpleGrantedAuthority role = new SimpleGrantedAuthority(
                appUser.getRole() != null ? appUser.getRole().name() : "ROLE_UNKNOWN"
        );
        List<SimpleGrantedAuthority> roles = List.of(role);
        return new AuthenticationToken(principal, null, "appUser", roles);
    }
}
