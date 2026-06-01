package com.gamesplatform.achievement.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 成就响应。
 */
@Data
@Builder
public class AchievementResponse {
    /**
     * 主键 ID。
     */
    private Long id;
    /**
     * 业务编码。
     */
    private String code;
    /**
     * 所属游戏编码。
     */
    private String gameCode;
    /**
     * 名称。
     */
    private String name;
    /**
     * 描述。
     */
    private String description;
    /**
     * 图标。
     */
    private String icon;
    /**
     * 是否已解锁。
     */
    private boolean unlocked;
    /**
     * 解锁时间。
     */
    private String unlockedAt;
}
