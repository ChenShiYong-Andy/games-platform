package com.gamesplatform.system.admin.security;

import com.gamesplatform.system.admin.entity.AdminAccount;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * 独立管理员访问令牌生成与校验组件。
 */
@Component
public class AdminJwtTokenProvider {

    /** 管理员令牌签名密钥。 */
    @Schema(description = "管理员令牌签名密钥")
    private final SecretKey key;
    /** 管理员令牌有效时长。 */
    @Schema(description = "管理员令牌有效时长")
    private final long expiration;

    /**
     * 创建管理员访问令牌组件。
     *
     * @param secret 管理员令牌签名密钥。
     * @param expiration 管理员令牌有效时长。
     */
    public AdminJwtTokenProvider(
            @Value("${admin.jwt.secret}") String secret,
            @Value("${admin.jwt.expiration}") long expiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    /**
     * 生成管理员访问令牌。
     *
     * @param admin 管理员账户。
     * @return 管理员访问令牌。
     */
    public String generateToken(AdminAccount admin) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(admin.getId()))
                .claim("username", admin.getUsername())
                .claim("tokenVersion", admin.getTokenVersion())
                .claim("actorType", "ADMIN")
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key)
                .compact();
    }

    /**
     * 读取管理员主键。
     *
     * @param token 管理员访问令牌。
     * @return 管理员主键。
     */
    public Long getAdminId(String token) {
        return Long.parseLong(parseClaims(token).getSubject());
    }

    /**
     * 读取令牌版本。
     *
     * @param token 管理员访问令牌。
     * @return 令牌版本。
     */
    public Integer getTokenVersion(String token) {
        return parseClaims(token).get("tokenVersion", Integer.class);
    }

    /**
     * 校验管理员访问令牌。
     *
     * @param token 管理员访问令牌。
     * @return 令牌有效时返回 {@code true}。
     */
    public boolean validateToken(String token) {
        try {
            return "ADMIN".equals(parseClaims(token).get("actorType", String.class));
        } catch (Exception exception) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
