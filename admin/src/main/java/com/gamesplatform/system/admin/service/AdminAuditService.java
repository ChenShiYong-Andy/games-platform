package com.gamesplatform.system.admin.service;

/** 管理员操作审计服务。 */
public interface AdminAuditService {
    /**
     * 记录管理员操作。
     * @param adminId 管理员主键。
     * @param userId 被管理用户主键。
     * @param module 业务模块。
     * @param action 操作名称。
     * @param before 操作前数据。
     * @param after 操作后数据。
     */
    void record(Long adminId, Long userId, String module, String action, Object before, Object after);
}
