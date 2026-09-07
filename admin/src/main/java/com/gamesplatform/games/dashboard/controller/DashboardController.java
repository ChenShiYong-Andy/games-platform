package com.gamesplatform.games.dashboard.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.games.dashboard.dto.TodayGameStatsResponse;
import com.gamesplatform.games.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 游戏大厅数据接口。
 */
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    /**
     * 游戏大厅数据服务
     */
    @Schema(description = "游戏大厅数据服务")
    private final DashboardService dashboardService;

    /**
     * 查询当前用户今日各类游戏的参与次数。
     *
     * @param authentication 当前认证信息
     * @return 今日游戏次数统计
     */
    @GetMapping("/today-game-stats")
    public ApiResponse<TodayGameStatsResponse> getTodayGameStats(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(dashboardService.getTodayGameStats(userId));
    }
}
