package com.gamesplatform.school.english.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 每日英语跟读任务状态响应。
 */
@Data
@AllArgsConstructor
public class DailyEnglishTaskStatusResponse {

    /**
     * 当天跟读任务是否已经完成。
     */
    @Schema(description = "当天跟读任务是否已经完成")
    private boolean completed;

    /**
     * 完成当天任务可获得的积分。
     */
    @Schema(description = "完成当天任务可获得的积分")
    private int rewardPoints;
}
