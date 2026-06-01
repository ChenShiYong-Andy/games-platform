package com.gamesplatform.user.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.user.dto.*;
import com.gamesplatform.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 认证接口。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    /**
     * 用户服务。
     */
    private final UserService userService;

    /**
     * 注册用户。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/register")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(userService.register(request));
    }

    /**
     * 用户登录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success(userService.login(request));
    }
}

/**
 * 用户资料接口。
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
class UserController {

    /**
     * 用户服务。
     */
    private final UserService userService;

    /**
     * 查询用户资料。
     *
     * @param authentication 当前认证信息。
     * @return 处理结果。
     */
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(userService.getProfile(userId));
    }

    /**
     * 更新用户资料。
     *
     * @param authentication 当前认证信息。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PutMapping("/profile")
    public ApiResponse<UserProfileResponse> updateProfile(
            Authentication authentication,
            @RequestBody UpdateProfileRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(userService.updateProfile(userId, request));
    }
}
