package com.gamesplatform.school.english.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 每日英语跟读任务结算响应。
 */
@Data
@AllArgsConstructor
public class DailyEnglishTaskCompletionResponse {

    /**
     * 本次请求是否首次完成任务。
     */
    @Schema(description = "本次请求是否首次完成任务")
    private boolean newlyCompleted;

    /**
     * 本次获得的积分。
     */
    @Schema(description = "本次获得的积分")
    private int pointsEarned;

    /**
     * 用户结算后的总积分。
     */
    @Schema(description = "用户结算后的总积分")
    private int totalPoints;
}
