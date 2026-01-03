package com.coffee.atom.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.coffee.atom.common.ApiResponse;
import com.coffee.atom.config.CodeValue;
import com.coffee.atom.config.error.ErrorValue;
import com.coffee.atom.config.error.ExceptionHandlerFilter;
import java.io.PrintWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@EnableWebSecurity
@Configuration
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final ExceptionHandlerFilter exceptionHandlerFilter;

    private static final String[] SWAGGER_URIS = {
            "/swagger-ui/**", "/api-docs",
            "/v3/api-docs/**", "/api-docs/**", "/swagger-ui.html",
            "/webjars/swagger-ui/**",
            "/account/signIn", "/account/signUp"
    };

    @Bean
    public AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
        return (request, response, e) -> {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            String json = objectMapper.writeValueAsString(
                    ApiResponse.builder()
                            .message(ErrorValue.ACCESS_DENIED.toString())
                            .code(CodeValue.ACCESS_DENIED.getValue())
                            .build());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            PrintWriter writer = response.getWriter();
            writer.write(json);
            writer.flush();
        };
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint(ObjectMapper objectMapper) {
        return (request, response, e) -> {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            String json = objectMapper.writeValueAsString(ApiResponse.builder()
                    .message(ErrorValue.UNAUTHORIZED.toString())
                    .code(CodeValue.NO_TOKEN_IN_REQUEST.getValue())
                    .build());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            PrintWriter writer = response.getWriter();
            writer.write(json);
            writer.flush();
        };
    }

    @Bean
    public HttpFirewall allowUrlEncodedSlashHttpFirewall() {
        DefaultHttpFirewall firewall = new DefaultHttpFirewall();
        firewall.setAllowUrlEncodedSlash(true);
        return firewall;
    }

    @Bean
    public SecurityFilterChain filterChain (HttpSecurity http,
                                            JwtAuthenticationFilter jwtAuthenticationFilter,
                                            AccessDeniedHandler accessDeniedHandler,
                                            AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(requests ->
                        requests
                                // OPTIONS 요청은 모두 허용 (CORS preflight)
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                
                                // Swagger 및 공개 엔드포인트
                                .requestMatchers(SWAGGER_URIS).permitAll()
                                .requestMatchers("/app-user/sign-in").permitAll()
                                
                                // AppUser 관련
                                .requestMatchers(HttpMethod.POST, "/app-user/sign-up", "/app-user/sign-up/url").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.PATCH, "/app-user", "/app-user/url").authenticated()
                                .requestMatchers(HttpMethod.GET, "/app-user/village-heads", "/app-user/village-head/**", "/app-user/my").authenticated()
                                .requestMatchers(HttpMethod.GET, "/app-user/vice-admins", "/app-user/vice-admin/**").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.PATCH, "/app-user/vice-admin/**").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/app-user/**").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/app-user/**").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/app-user/password").hasAnyAuthority("ADMIN", "USER")
                                
                                // Approval 관련 - POST (생성 요청)
                                .requestMatchers(HttpMethod.POST, "/approval/village-head", "/approval/village-head/url",
                                        "/approval/farmer", "/approval/farmer/url",
                                        "/approval/purchase", "/approval/section").hasAnyAuthority("VICE_ADMIN_HEAD_OFFICER", "VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER", "ADMIN")
                                
                                // Approval 관련 - PATCH (수정/승인/거절) - 더 구체적인 경로를 먼저 매칭
                                .requestMatchers(HttpMethod.PATCH, "/approval/farmer/**").hasAnyAuthority("VICE_ADMIN_HEAD_OFFICER", "VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER", "ADMIN")
                                .requestMatchers(HttpMethod.PATCH, "/approval/purchase/**").hasAnyAuthority("VICE_ADMIN_HEAD_OFFICER", "VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER", "ADMIN")
                                // [NEW] Query Parameter 지원을 위한 추가 설정 (2026-01-03)
                                // 롤백 시: 아래 라인을 주석 처리
                                .requestMatchers(HttpMethod.PATCH, "/approval/purchase").hasAnyAuthority("VICE_ADMIN_HEAD_OFFICER", "VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER", "ADMIN")
                                // [ROLLBACK] 기존에는 위의 "/approval/purchase" 라인이 없었음
                                .requestMatchers(HttpMethod.PATCH, "/approval/approve/**", "/approval/reject/**").hasAnyAuthority("VICE_ADMIN_HEAD_OFFICER", "VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER", "ADMIN")
                                .requestMatchers(HttpMethod.PATCH, "/approval/village-head", "/approval/village-head/url").hasAnyAuthority("VICE_ADMIN_HEAD_OFFICER", "VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER", "ADMIN")
                                
                                // Approval 관련 - DELETE
                                .requestMatchers(HttpMethod.DELETE, "/approval/**").hasAnyAuthority("VICE_ADMIN_HEAD_OFFICER", "VICE_ADMIN_AGRICULTURE_MINISTRY_OFFICER", "ADMIN")
                                
                                // Approval 관련 - GET (조회)
                                .requestMatchers(HttpMethod.GET, "/approval/**").authenticated()
                                
                                // Purchase 관련
                                .requestMatchers(HttpMethod.GET, "/purchase/**").authenticated()
                                
                                // Section 관련
                                .requestMatchers(HttpMethod.POST, "/section").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/section/**").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/section/**").authenticated()
                                
                                // Area 관련
                                .requestMatchers(HttpMethod.POST, "/area").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/area/**").hasAuthority("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/area/**").authenticated()
                                
                                // Farmer 관련
                                .requestMatchers(HttpMethod.GET, "/farmer/**").authenticated()
                                
                                // File Event 관련
                                .requestMatchers(HttpMethod.GET, "/file-event/**").authenticated()
                                
                                // GCS 관련
                                .requestMatchers(HttpMethod.POST, "/gcs/**").authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/gcs/**").authenticated()
                                .requestMatchers(HttpMethod.GET, "/gcs/**").authenticated()
                                
                                // 기타 모든 요청은 인증 필요
                                .anyRequest().authenticated()
                ).cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(exceptionHandlerFilter, JwtAuthenticationFilter.class)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(it -> {
                    it.accessDeniedHandler(accessDeniedHandler);
                    it.authenticationEntryPoint(authenticationEntryPoint);
                })
            .httpBasic().disable()
            .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowedMethods(List.of(
                HttpMethod.GET.name(),
                HttpMethod.HEAD.name(),
                HttpMethod.POST.name(),
                HttpMethod.PUT.name(),
                HttpMethod.DELETE.name(),
                HttpMethod.PATCH.name(),
                HttpMethod.OPTIONS.name()
        ));
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
