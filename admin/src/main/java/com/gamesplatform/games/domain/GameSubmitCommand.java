package com.gamesplatform.games.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 游戏提交命令。
 */
@Data
public class GameSubmitCommand {

    /**
     * 游戏 ID。
     */
    @Schema(description = "游戏 ID")
    private Long gameId;
    /**
     * 用户 ID。
     */
    @Schema(description = "用户 ID")
    private Long userId;
    /**
     * 当前棋盘。
     */
    @Schema(description = "当前棋盘")
    private int[][] board;
    /**
     * 耗时秒数。
     */
    @Schema(description = "耗时秒数")
    private int elapsedSeconds;
    /**
     * 已使用提示次数。
     */
    @Schema(description = "已使用提示次数")
    private int hintsUsed;
    /**
     * 错误次数。
     */
    @Schema(description = "错误次数")
    private int mistakes;
}
