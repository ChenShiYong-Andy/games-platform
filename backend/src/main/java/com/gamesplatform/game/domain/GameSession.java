package com.gamesplatform.game.domain;

import lombok.Builder;
import lombok.Data;

/**
 * 游戏会话。
 */
@Data
@Builder
public class GameSession {

    /**
     * 游戏 ID。
     */
    private Long gameId;
    /**
     * 游戏类型。
     */
    private String gameType;
    /**
     * 游戏难度。
     */
    private String difficulty;
    /**
     * 棋盘尺寸。
     */
    private Integer gridSize;
    /**
     * 题目棋盘。
     */
    private int[][] puzzle;
    /**
     * 答案棋盘。
     */
    private int[][] solution;
}
