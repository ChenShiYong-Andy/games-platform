package com.gamesplatform.zoo.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 动物园照料响应。
 */
@Data
@Builder
public class ZooCareResponse {

    /**
     * 游戏 ID。
     */
    private Long gameId;
    /**
     * 状态。
     */
    private String status;
    /**
     * 当前照料需求。
     */
    private String currentNeed;
    /**
     * 得分。
     */
    private Integer score;
    /**
     * 剩余秒数。
     */
    private Integer remainingSeconds;
    /**
     * 今日剩余照料次数。
     */
    private Integer remainingToday;
    /**
     * 已获得积分。
     */
    private Integer pointsAwarded;
    /**
     * 已扣除积分。
     */
    private Integer pointsDeducted;
    /**
     * 累计积分。
     */
    private Integer totalPoints;
    /**
     * 本次照料是否正确。
     */
    private Boolean correct;
    /**
     * 提示信息。
     */
    private String message;
}
