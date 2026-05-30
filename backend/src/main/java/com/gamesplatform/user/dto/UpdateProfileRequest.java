package com.gamesplatform.user.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {

    private String nickname;
    private String email;
    private String avatarUrl;
}
