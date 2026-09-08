package com.gamesplatform.school.english.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.school.english.dto.DailyEnglishResponse;
import com.gamesplatform.school.english.dto.DailyEnglishTaskCompletionRequest;
import com.gamesplatform.school.english.dto.DailyEnglishTaskCompletionResponse;
import com.gamesplatform.school.english.dto.DailyEnglishTaskStatusResponse;
import com.gamesplatform.school.english.service.DailyEnglishService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 每日英语接口。
 */
@RestController
@RequestMapping("/api/daily-english")
@RequiredArgsConstructor
public class DailyEnglishController {

    /**
     * 每日英语口语练习服务。
     */
    @Schema(description = "每日英语口语练习服务")
    private final DailyEnglishService dailyEnglishService;

    /**
     * 获取当天的英语口语练习；当天同一年级的内容优先从缓存读取。
     *
     * @param authentication 当前认证信息。
     * @return 当天英语口语练习响应。
     */
    @GetMapping("/today")
    public ApiResponse<DailyEnglishResponse> getTodayPractice(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(dailyEnglishService.getTodayPractice(userId));
    }

    /**
     * 查询当前用户当天的跟读任务完成状态。
     *
     * @param authentication 当前认证信息。
     * @return 当天跟读任务状态。
     */
    @GetMapping("/today/status")
    public ApiResponse<DailyEnglishTaskStatusResponse> getTodayTaskStatus(
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(dailyEnglishService.getTodayTaskStatus(userId));
    }

    /**
     * 完成并结算当前用户当天的全部跟读任务。
     *
     * @param authentication 当前认证信息。
     * @param request 已完成的跟读任务内容。
     * @return 跟读任务结算结果。
     */
    @PostMapping("/today/complete")
    public ApiResponse<DailyEnglishTaskCompletionResponse> completeTodayTask(
            Authentication authentication,
            @Valid @RequestBody DailyEnglishTaskCompletionRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(dailyEnglishService.completeTodayTask(userId, request));
    }
}
