package com.gamesplatform.system.admin.service;

import com.gamesplatform.school.english.dto.DailyEnglishConfigRequest;
import com.gamesplatform.school.english.dto.DailyEnglishConfigResponse;

/** 管理端用户每日英语配置服务。 */
public interface AdminEnglishConfigService {
    /**
     * 查询用户的有效配置。
     *
     * @param adminId 管理员主键
     * @param userId 用户主键
     * @return 每日英语配置
     */
    DailyEnglishConfigResponse getConfig(Long adminId, Long userId);

    /**
     * 保存用户配置。
     *
     * @param adminId 管理员主键
     * @param userId 用户主键
     * @param request 配置请求
     * @return 每日英语配置
     */
    DailyEnglishConfigResponse updateConfig(Long adminId, Long userId, DailyEnglishConfigRequest request);

    /**
     * 删除用户配置并恢复系统默认值。
     *
     * @param adminId 管理员主键
     * @param userId 用户主键
     * @return 系统默认配置
     */
    DailyEnglishConfigResponse resetConfig(Long adminId, Long userId);
}
