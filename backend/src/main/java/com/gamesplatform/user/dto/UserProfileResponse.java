package com.gamesplatform.user.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserProfileResponse {

    private Long id;
    private String username;
    private String nickname;
    private String avatarUrl;
    private String email;
    private Integer level;
    private Integer totalPoints;
    private Integer loginStreak;
    private Integer totalClears;
}
