package com.coffee.atom.config.security;

import com.coffee.atom.config.CodeValue;
import com.coffee.atom.config.error.CustomException;
import com.coffee.atom.config.error.ErrorValue;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtProvider {

    @Value("${spring.jwt.secret-key}")
    private String secretKey;

    public String createAccessToken(Long appUserId) {
        return createToken("appUserId", appUserId);
    }

    private String createToken(String key, Long appUserId) {

        Map<String, Object> claims = getClaims(key, appUserId);

        Date now = new Date();
        Date expiryDate = Date.from(now.toInstant().plus(Duration.ofDays(1)));

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(createSignKey())
                .compact();
    }

    private Map<String, Object> getClaims(String key, Long appUserId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(key, appUserId.toString());
        return claims;
    }

    public Claims verifyToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(createSignKey().getEncoded())    // 비밀값으로 복호화
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorValue.TOKEN_EXPIRED, CodeValue.NO_TOKEN);
        } catch (JwtException e) {
            log.error("e : ",e);
            throw new CustomException(ErrorValue.JWT_PARSING_ERROR);
        }
    }

    private SecretKey createSignKey() {
        return Keys.hmacShaKeyFor(this.secretKey.getBytes(StandardCharsets.UTF_8));
    }
}
