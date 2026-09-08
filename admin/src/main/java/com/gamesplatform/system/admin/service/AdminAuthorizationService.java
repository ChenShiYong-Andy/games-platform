package com.gamesplatform.system.admin.service;

/** 管理端关联用户访问授权服务。 */
public interface AdminAuthorizationService {
    /**
     * 校验管理员是否关联指定用户。
     *
     * @param adminId 管理员主键
     * @param userId 用户主键
     */
    void requireLinkedUser(Long adminId, Long userId);
}
