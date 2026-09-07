package com.gamesplatform.school.english.service;

import com.gamesplatform.school.english.dto.DailyEnglishResponse;
import com.gamesplatform.school.english.dto.DailyEnglishTaskCompletionRequest;
import com.gamesplatform.school.english.dto.DailyEnglishTaskCompletionResponse;
import com.gamesplatform.school.english.dto.DailyEnglishTaskStatusResponse;

/**
 * 每日英语业务服务。
 */
public interface DailyEnglishService {

    /**
     * 获取当天的英语口语练习。
     *
     * @return 当天英语口语练习。
     */
    DailyEnglishResponse getTodayPractice();

    /**
     * 查询当前用户当天的跟读任务状态。
     *
     * @param userId 当前用户 ID。
     * @return 当天任务状态及奖励积分。
     */
    DailyEnglishTaskStatusResponse getTodayTaskStatus(Long userId);

    /**
     * 校验并结算当前用户当天的全部跟读任务。
     *
     * @param userId 当前用户 ID。
     * @param request 已完成的跟读任务内容。
     * @return 跟读任务结算结果。
     */
    DailyEnglishTaskCompletionResponse completeTodayTask(
            Long userId,
            DailyEnglishTaskCompletionRequest request);
}
