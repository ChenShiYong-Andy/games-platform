package com.gamesplatform.admin.controller;

import com.gamesplatform.admin.dto.AdminConfigStatusResponse;
import com.gamesplatform.admin.dto.AdminGameConfigRequest;
import com.gamesplatform.admin.dto.AdminPointsAdjustRequest;
import com.gamesplatform.admin.dto.SetAdminPasswordRequest;
import com.gamesplatform.admin.service.AdminConfigService;
import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.user.dto.UserProfileResponse;
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
 * 管理员配置接口。
 */
@RestController
@RequestMapping("/api/admin/config")
@RequiredArgsConstructor
public class AdminConfigController {

    /**
     * 管理员配置服务。
     */
    private final AdminConfigService adminConfigService;

    /**
     * 查询管理配置状态。
     *
     * @param authentication 当前认证信息。
     * @return 处理结果。
     */
    @GetMapping("/status")
    public ApiResponse<AdminConfigStatusResponse> getStatus(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(adminConfigService.getStatus(userId));
    }

    /**
     * 设置管理密码。
     *
     * @param authentication 当前认证信息。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/password")
    public ApiResponse<AdminConfigStatusResponse> setPassword(
            Authentication authentication,
            @Valid @RequestBody SetAdminPasswordRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(adminConfigService.setPassword(userId, request));
    }

    /**
     * 更新游戏配置。
     *
     * @param authentication 当前认证信息。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PutMapping("/game")
    public ApiResponse<UserProfileResponse> updateGameConfig(
            Authentication authentication,
            @Valid @RequestBody AdminGameConfigRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(adminConfigService.updateGameConfig(userId, request));
    }

    /**
     * 调整积分。
     *
     * @param authentication 当前认证信息。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/points")
    public ApiResponse<UserProfileResponse> adjustPoints(
            Authentication authentication,
            @Valid @RequestBody AdminPointsAdjustRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(adminConfigService.adjustPoints(userId, request));
    }
}
