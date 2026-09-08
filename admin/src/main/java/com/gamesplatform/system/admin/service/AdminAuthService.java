package com.gamesplatform.system.admin.service;

import com.gamesplatform.system.admin.dto.AdminAuthResponse;
import com.gamesplatform.system.admin.dto.AdminLoginRequest;
import com.gamesplatform.system.admin.dto.AdminProfileResponse;
import com.gamesplatform.system.admin.dto.AdminRegisterRequest;

/** 管理员独立认证服务。 */
public interface AdminAuthService {
    /**
     * 注册管理员。
     *
     * @param request 注册请求
     * @return 认证响应
     */
    AdminAuthResponse register(AdminRegisterRequest request);

    /**
     * 登录管理员。
     *
     * @param request 登录请求
     * @return 认证响应
     */
    AdminAuthResponse login(AdminLoginRequest request);

    /**
     * 查询管理员资料。
     *
     * @param adminId 管理员主键
     * @return 管理员资料
     */
    AdminProfileResponse getProfile(Long adminId);
}
