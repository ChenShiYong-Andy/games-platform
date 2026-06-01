package com.gamesplatform.achievement.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.achievement.dto.AchievementResponse;
import com.gamesplatform.achievement.service.AchievementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 成就接口。
 */
@RestController
@RequestMapping("/api/achievements")
@RequiredArgsConstructor
public class AchievementController {

    /**
     * 成就服务。
     */
    private final AchievementService achievementService;

    /**
     * 查询用户成就。
     *
     * @param authentication 当前认证信息。
     * @param gameCode 游戏编码。
     * @return 处理结果。
     */
    @GetMapping
    public ApiResponse<List<AchievementResponse>> getAchievements(
            Authentication authentication,
            @RequestParam(required = false) String gameCode) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(achievementService.getUserAchievements(userId, gameCode));
    }
}
