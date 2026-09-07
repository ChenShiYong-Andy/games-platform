package com.gamesplatform.games.dashboard.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 当前用户今日游戏次数。
 */
@Data
@Builder
public class TodayGameStatsResponse {

    /**
     * 今日数独游戏次数
     */
    @Schema(description = "今日数独游戏次数")
    private Long sudoku;

    /**
     * 今日五子棋游戏次数
     */
    @Schema(description = "今日五子棋游戏次数")
    private Long gomoku;

    /**
     * 今日中国象棋游戏次数
     */
    @Schema(description = "今日中国象棋游戏次数")
    private Long chess;
}
