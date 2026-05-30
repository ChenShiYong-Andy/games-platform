package com.gamesplatform.achievement.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AchievementResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private String icon;
    private boolean unlocked;
    private String unlockedAt;
}
