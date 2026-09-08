package com.gamesplatform.system.admin.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.school.english.dto.DailyEnglishConfigRequest;
import com.gamesplatform.school.english.dto.DailyEnglishConfigResponse;
import com.gamesplatform.system.admin.dto.AdminBindUserRequest;
import com.gamesplatform.system.admin.dto.AdminPetGrowthAdjustRequest;
import com.gamesplatform.system.admin.dto.AdminPointsAdjustRequest;
import com.gamesplatform.system.admin.dto.ManagedUserResponse;
import com.gamesplatform.system.admin.service.AdminBindingService;
import com.gamesplatform.system.admin.service.AdminConfigService;
import com.gamesplatform.system.admin.service.AdminEnglishConfigService;
import com.gamesplatform.system.user.dto.UserProfileResponse;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 管理员工作台的关联用户管理接口。 */
@RestController
@RequestMapping("/api/admin/managed-users")
@RequiredArgsConstructor
public class AdminManagedUserController {
    /** 管理员用户关联服务。 */
    @Schema(description = "管理员用户关联服务")
    private final AdminBindingService bindingService;
    /** 管理端用户配置服务。 */
    @Schema(description = "管理端用户配置服务")
    private final AdminConfigService configService;
    /** 管理端每日英语配置服务。 */
    @Schema(description = "管理端每日英语配置服务")
    private final AdminEnglishConfigService englishConfigService;

    /** 查询已关联用户。 @param authentication 当前认证。 @return 已关联用户列表。 */
    @GetMapping
    public ApiResponse<List<ManagedUserResponse>> list(Authentication authentication) {
        return ApiResponse.success(bindingService.listManagedUsers(adminId(authentication)));
    }

    /** 通过一次性绑定码关联用户。 @param authentication 当前认证。 @param request 绑定请求。 @return 已关联用户。 */
    @PostMapping("/bind")
    public ApiResponse<ManagedUserResponse> bind(
            Authentication authentication, @Valid @RequestBody AdminBindUserRequest request) {
        return ApiResponse.success(bindingService.bindUser(adminId(authentication), request));
    }

    /** 解除用户关联。 @param authentication 当前认证。 @param userId 用户主键。 @return 操作结果。 */
    @DeleteMapping("/{userId}")
    public ApiResponse<String> unbind(Authentication authentication, @PathVariable Long userId) {
        bindingService.unbindUser(adminId(authentication), userId);
        return ApiResponse.success("已解除关联");
    }

    /** 调整用户积分。 @param authentication 当前认证。 @param userId 用户主键。 @param request 调整请求。 @return 用户资料。 */
    @PostMapping("/{userId}/points/adjust")
    public ApiResponse<UserProfileResponse> adjustPoints(
            Authentication authentication,
            @PathVariable Long userId,
            @Valid @RequestBody AdminPointsAdjustRequest request) {
        return ApiResponse.success(configService.adjustPoints(adminId(authentication), userId, request));
    }

    /** 扣减宠物成长值。 @param authentication 当前认证。 @param userId 用户主键。 @param request 调整请求。 @return 操作结果。 */
    @PostMapping("/{userId}/pet/growth/deduct")
    public ApiResponse<String> deductPetGrowth(
            Authentication authentication,
            @PathVariable Long userId,
            @Valid @RequestBody AdminPetGrowthAdjustRequest request) {
        configService.deductPetGrowth(adminId(authentication), userId, request);
        return ApiResponse.success("扣减成功");
    }

    /** 查询用户每日英语配置。 @param authentication 当前认证。 @param userId 用户主键。 @return 每日英语配置。 */
    @GetMapping("/{userId}/daily-english/config")
    public ApiResponse<DailyEnglishConfigResponse> getEnglishConfig(
            Authentication authentication, @PathVariable Long userId) {
        return ApiResponse.success(englishConfigService.getConfig(adminId(authentication), userId));
    }

    /** 保存用户每日英语配置。 @param authentication 当前认证。 @param userId 用户主键。 @param request 配置请求。 @return 每日英语配置。 */
    @PutMapping("/{userId}/daily-english/config")
    public ApiResponse<DailyEnglishConfigResponse> updateEnglishConfig(
            Authentication authentication,
            @PathVariable Long userId,
            @Valid @RequestBody DailyEnglishConfigRequest request) {
        return ApiResponse.success(englishConfigService.updateConfig(
                adminId(authentication), userId, request));
    }

    /** 恢复用户每日英语系统默认配置。 @param authentication 当前认证。 @param userId 用户主键。 @return 默认配置。 */
    @DeleteMapping("/{userId}/daily-english/config")
    public ApiResponse<DailyEnglishConfigResponse> resetEnglishConfig(
            Authentication authentication, @PathVariable Long userId) {
        return ApiResponse.success(englishConfigService.resetConfig(adminId(authentication), userId));
    }

    private Long adminId(Authentication authentication) {
        return (Long) authentication.getPrincipal();
    }
}
