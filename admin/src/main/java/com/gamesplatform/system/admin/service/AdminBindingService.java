package com.gamesplatform.system.admin.service;

import com.gamesplatform.system.admin.dto.AdminBindUserRequest;
import com.gamesplatform.system.admin.dto.AdminBindingCodeResponse;
import com.gamesplatform.system.admin.dto.AdminBindingStatusResponse;
import com.gamesplatform.system.admin.dto.ManagedUserResponse;

import java.util.List;

/** 管理员与普通用户关联服务。 */
public interface AdminBindingService {
    /**
     * 生成一次性绑定码。
     *
     * @param userId 用户主键
     * @return 绑定码
     */
    AdminBindingCodeResponse generateBindingCode(Long userId);

    /**
     * 查询用户关联状态。
     *
     * @param userId 用户主键
     * @return 关联状态
     */
    AdminBindingStatusResponse getBindingStatus(Long userId);

    /**
     * 管理员绑定用户。
     *
     * @param adminId 管理员主键
     * @param request 绑定请求
     * @return 已关联用户
     */
    ManagedUserResponse bindUser(Long adminId, AdminBindUserRequest request);

    /**
     * 查询管理员关联的用户。
     *
     * @param adminId 管理员主键
     * @return 用户列表
     */
    List<ManagedUserResponse> listManagedUsers(Long adminId);

    /**
     * 解除管理员与用户的关联。
     *
     * @param adminId 管理员主键
     * @param userId 用户主键
     */
    void unbindUser(Long adminId, Long userId);
}
