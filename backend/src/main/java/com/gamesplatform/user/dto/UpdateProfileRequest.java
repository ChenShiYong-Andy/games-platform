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
    /**
     * 每日数独次数上限。
     */
    private Integer sudokuDailyLimit;
    /**
     * 每日动物园照料次数上限。
     */
    private Integer zooDailyCareLimit;
}
