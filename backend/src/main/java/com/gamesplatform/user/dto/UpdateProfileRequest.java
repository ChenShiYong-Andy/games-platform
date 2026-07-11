package com.gamesplatform.user.dto;

import lombok.Data;

/**
 * 用户资料更新请求。
 */
@Data
public class UpdateProfileRequest {

    /**
     * 昵称。
     */
    private String nickname;
    /**
     * 邮箱地址。
     */
    private String email;
    /**
     * 头像地址。
     */
    private String avatarUrl;
}
