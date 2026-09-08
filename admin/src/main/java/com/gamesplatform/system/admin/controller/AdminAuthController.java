package com.gamesplatform.system.admin.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.system.admin.dto.AdminAuthResponse;
import com.gamesplatform.system.admin.dto.AdminLoginRequest;
import com.gamesplatform.system.admin.dto.AdminProfileResponse;
import com.gamesplatform.system.admin.dto.AdminRegisterRequest;
import com.gamesplatform.system.admin.service.AdminAuthService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 独立管理员认证接口。 */
@RestController
@RequestMapping("/api/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {
    /** 管理员认证服务。 */
    @Schema(description = "管理员认证服务")
    private final AdminAuthService adminAuthService;

    /** 注册管理员。 @param request 注册请求。 @return 管理员认证响应。 */
    @PostMapping("/register")
    public ApiResponse<AdminAuthResponse> register(@Valid @RequestBody AdminRegisterRequest request) {
        return ApiResponse.success(adminAuthService.register(request));
    }

    /** 登录管理员。 @param request 登录请求。 @return 管理员认证响应。 */
    @PostMapping("/login")
    public ApiResponse<AdminAuthResponse> login(@Valid @RequestBody AdminLoginRequest request) {
        return ApiResponse.success(adminAuthService.login(request));
    }

    /** 查询当前管理员资料。 @param authentication 当前认证。 @return 管理员资料。 */
    @GetMapping("/profile")
    public ApiResponse<AdminProfileResponse> profile(Authentication authentication) {
        return ApiResponse.success(adminAuthService.getProfile((Long) authentication.getPrincipal()));
    }
}
