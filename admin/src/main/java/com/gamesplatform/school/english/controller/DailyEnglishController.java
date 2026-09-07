package com.gamesplatform.school.english.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.school.english.dto.DailyEnglishResponse;
import com.gamesplatform.school.english.service.DailyEnglishService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
     * @return 当天英语口语练习响应。
     */
    @GetMapping("/today")
    public ApiResponse<DailyEnglishResponse> getTodayPractice() {
        return ApiResponse.success(dailyEnglishService.getTodayPractice());
    }
}
