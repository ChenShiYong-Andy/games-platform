package com.gamesplatform.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 认证响应。
 */
@Data
@Builder
public class AuthResponse {

    /**
     * 访问令牌。
     */
    private String token;
    /**
     * 用户资料。
     */
    private UserProfileResponse user;
}
