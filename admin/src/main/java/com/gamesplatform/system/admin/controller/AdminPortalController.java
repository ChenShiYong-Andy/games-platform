package com.gamesplatform.system.admin.controller;

import com.gamesplatform.system.admin.dto.AdminPortalPasswordRequest;
import com.gamesplatform.system.admin.dto.AdminPortalStatusResponse;
import com.gamesplatform.system.admin.dto.AdminPetGrowthAdjustRequest;
import com.gamesplatform.system.admin.dto.AdminPointsAdjustRequest;
import com.gamesplatform.system.admin.service.AdminConfigService;
import com.gamesplatform.system.admin.service.AdminPortalService;
import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.school.english.dto.DailyEnglishConfigRequest;
import com.gamesplatform.school.english.dto.DailyEnglishConfigResponse;
import com.gamesplatform.system.user.dto.UserProfileResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 独立管理后台接口。
 */
@RestController
@RequestMapping("/api/admin-portal")
@RequiredArgsConstructor
public class AdminPortalController {

    private final AdminPortalService adminPortalService;
    private final AdminConfigService adminConfigService;

    @GetMapping("/status")
    public ApiResponse<AdminPortalStatusResponse> getStatus() {
        return ApiResponse.success(adminPortalService.getStatus());
    }

    @PostMapping("/password")
    public ApiResponse<AdminPortalStatusResponse> setPassword(
            @Valid @RequestBody AdminPortalPasswordRequest request) {
        return ApiResponse.success(adminPortalService.setPassword(request));
    }

    @PostMapping("/verify")
    public ApiResponse<String> verify(@Valid @RequestBody AdminPortalPasswordRequest request) {
        adminPortalService.verifyPassword(request.getPassword());
        return ApiResponse.success("验证成功");
    }

    @PostMapping("/daily-english/config")
    public ApiResponse<DailyEnglishConfigResponse> getEnglishConfig(
            @Valid @RequestBody AdminPortalPasswordRequest request) {
        return ApiResponse.success(adminPortalService.getEnglishConfig(request));
    }

    @PutMapping("/daily-english/config")
    public ApiResponse<DailyEnglishConfigResponse> updateEnglishConfig(
            @Valid @RequestBody DailyEnglishConfigRequest request) {
        return ApiResponse.success(adminPortalService.updateEnglishConfig(request));
    }

    @PostMapping("/points")
    public ApiResponse<UserProfileResponse> adjustPoints(
            Authentication authentication,
            @Valid @RequestBody AdminPointsAdjustRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(adminConfigService.adjustPoints(userId, request));
    }

    @PostMapping("/pet/growth/deduct")
    public ApiResponse<String> deductPetGrowth(
            Authentication authentication,
            @Valid @RequestBody AdminPetGrowthAdjustRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        adminConfigService.deductPetGrowth(userId, request);
        return ApiResponse.success("扣减成功");
    }
}
