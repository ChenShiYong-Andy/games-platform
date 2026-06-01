package com.gamesplatform.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 令牌生成与校验组件。
 */
@Component
public class JwtTokenProvider {

    /**
     * 签名密钥。
     */
    private final SecretKey key;
    /**
     * 令牌有效期。
     */
    private final long expiration;

    /**
     * 创建 JWT 令牌生成与校验组件。
     *
     * @param secret 签名密钥。
     * @param expiration 令牌有效期。
     */
    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * 生成访问令牌。
     *
     * @param userId 用户 ID。
     * @param username 用户名。
     * @return 处理结果。
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(key)
                .compact();
    }

    /**
     * 从令牌中读取用户 ID。
     *
     * @param token 访问令牌。
     * @return 处理结果。
     */
    public Long getUserId(String token) {
        Claims claims = parseClaims(token);
        return Long.parseLong(claims.getSubject());
    }

    /**
     * 校验访问令牌。
     *
     * @param token 访问令牌。
     * @return 处理结果。
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
