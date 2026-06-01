package com.gamesplatform.game.domain;

import lombok.Builder;
import lombok.Data;

/**
 * 游戏结算结果。
 */
@Data
@Builder
public class GameResult {

    /**
     * 是否成功。
     */
    private boolean success;
    /**
     * 得分。
     */
    private int score;
    /**
     * 获得的积分。
     */
    private int pointsEarned;
    /**
     * 提示信息。
     */
    private String message;
}
