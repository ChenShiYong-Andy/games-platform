package com.gamesplatform.user.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 用户资料响应。
 */
@Data
@Builder
public class UserProfileResponse {

    /**
     * 主键 ID。
     */
    private Long id;
    /**
     * 用户名。
     */
    private String username;
    /**
     * 昵称。
     */
    private String nickname;
    /**
     * 头像地址。
     */
    private String avatarUrl;
    /**
     * 邮箱地址。
     */
    private String email;
    /**
     * 用户等级。
     */
    private Integer level;
    /**
     * 累计积分。
     */
    private Integer totalPoints;
    /**
     * 连续登录天数。
     */
    private Integer loginStreak;
    /**
     * 累计通关次数。
     */
    private Integer totalClears;
    /**
     * 每日数独次数上限。
     */
    private Integer sudokuDailyLimit;
    /**
     * 是否已设置管理密码。
     */
    private Boolean adminPasswordSet;
}
