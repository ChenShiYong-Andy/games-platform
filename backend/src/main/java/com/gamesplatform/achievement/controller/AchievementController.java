package com.gamesplatform.achievement.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.achievement.dto.AchievementResponse;
import com.gamesplatform.achievement.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    @GetMapping
    public ApiResponse<List<AchievementResponse>> getAchievements(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(achievementService.getUserAchievements(userId));
    }
}
