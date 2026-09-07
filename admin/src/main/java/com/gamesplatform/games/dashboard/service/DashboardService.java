package com.gamesplatform.games.dashboard.service;

import com.gamesplatform.games.dashboard.dto.TodayGameStatsResponse;

/**
 * 游戏大厅数据业务服务。
 */
public interface DashboardService {

    /**
     * 查询当前用户今日各游戏的游玩次数。
     *
     * @param userId 当前用户 ID。
     * @return 今日游戏次数统计。
     */
    TodayGameStatsResponse getTodayGameStats(Long userId);
}
