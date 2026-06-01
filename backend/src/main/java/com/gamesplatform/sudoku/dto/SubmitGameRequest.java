package com.gamesplatform.sudoku.dto;

import lombok.Data;

/**
 * 数独游戏提交请求。
 */
@Data
public class SubmitGameRequest {

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
