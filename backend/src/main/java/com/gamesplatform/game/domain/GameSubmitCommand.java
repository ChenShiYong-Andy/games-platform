package com.gamesplatform.game.domain;

import lombok.Data;

/**
 * 游戏提交命令。
 */
@Data
public class GameSubmitCommand {

    /**
     * 游戏 ID。
     */
    private Long gameId;
    /**
     * 用户 ID。
     */
    private Long userId;
    /**
     * 当前棋盘。
     */
    private int[][] board;
    /**
     * 耗时秒数。
     */
    private int elapsedSeconds;
    /**
     * 已使用提示次数。
     */
    private int hintsUsed;
    /**
     * 错误次数。
     */
    private int mistakes;
}
