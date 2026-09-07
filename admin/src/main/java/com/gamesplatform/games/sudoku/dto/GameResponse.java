package com.gamesplatform.games.sudoku.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;

/**
 * 数独游戏响应。
 */
@Data
@Builder
public class GameResponse {

    /**
     * 主键 ID。
     */
    @Schema(description = "主键 ID")
    private Long id;
    /**
     * 游戏难度。
     */
    @Schema(description = "游戏难度")
    private String difficulty;
    /**
     * 棋盘尺寸。
     */
    @Schema(description = "棋盘尺寸")
    private Integer gridSize;
    /**
     * 题目棋盘。
     */
    @Schema(description = "题目棋盘")
    private int[][] puzzle;
    /**
     * 状态。
     */
    @Schema(description = "状态")
    private String status;
    /**
     * 耗时秒数。
     */
    @Schema(description = "耗时秒数")
    private Integer elapsedSeconds;
    /**
     * 已使用提示次数。
     */
    @Schema(description = "已使用提示次数")
    private Integer hintsUsed;
    /**
     * 错误次数。
     */
    @Schema(description = "错误次数")
    private Integer mistakes;
    /**
     * 得分。
     */
    @Schema(description = "得分")
    private Integer score;
    /**
     * 开始时间。
     */
    @Schema(description = "开始时间")
    private String startedAt;
    /**
     * 完成时间。
     */
    @Schema(description = "完成时间")
    private String completedAt;
}
