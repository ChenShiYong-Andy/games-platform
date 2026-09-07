package com.gamesplatform.system.user.service;

import com.gamesplatform.system.user.dto.AuthResponse;
import com.gamesplatform.system.user.dto.LoginRequest;
import com.gamesplatform.system.user.dto.RegisterRequest;
import com.gamesplatform.system.user.dto.UpdateProfileRequest;
import com.gamesplatform.system.user.dto.UserProfileResponse;
import com.gamesplatform.system.user.entity.User;

/**
 * 用户业务服务。
 */
public interface UserService {

    /**
     * 注册新用户。
     *
     * @param request 注册请求。
     * @return 登录认证信息。
     */
    AuthResponse register(RegisterRequest request);

    /**
     * 用户登录。
     *
     * @param request 登录请求。
     * @return 登录认证信息。
     */
    AuthResponse login(LoginRequest request);

    /**
     * 查询用户资料。
     *
     * @param userId 用户 ID。
     * @return 用户资料。
     */
    UserProfileResponse getProfile(Long userId);

    /**
     * 更新用户资料。
     *
     * @param userId 用户 ID。
     * @param request 用户资料更新请求。
     * @return 更新后的用户资料。
     */
    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

    /**
     * 根据 ID 查询用户实体。
     *
     * @param userId 用户 ID。
     * @return 用户实体。
     */
    User getUserById(Long userId);

    /**
     * 根据总积分计算用户等级。
     *
     * @param totalPoints 用户总积分。
     * @return 用户等级。
     */
    int calculateLevel(int totalPoints);
}
