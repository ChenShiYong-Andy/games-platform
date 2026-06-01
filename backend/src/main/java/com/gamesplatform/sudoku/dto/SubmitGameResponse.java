package com.gamesplatform.sudoku.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 数独游戏提交响应。
 */
@Data
@Builder
public class SubmitGameResponse {
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
    /**
     * 更新后的用户等级。
     */
    private Integer newLevel;
    /**
     * 累计积分。
     */
    private Integer totalPoints;
}
