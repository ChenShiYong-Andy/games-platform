package com.gamesplatform.system.admin.service;

import com.gamesplatform.school.english.dto.DailyEnglishConfigRequest;
import com.gamesplatform.school.english.dto.DailyEnglishConfigResponse;
import com.gamesplatform.system.admin.dto.AdminPortalPasswordRequest;
import com.gamesplatform.system.admin.dto.AdminPortalStatusResponse;

/**
 * 独立管理后台业务服务。
 */
public interface AdminPortalService {

    /**
     * 查询管理后台初始化状态。
     *
     * @return 管理后台状态。
     */
    AdminPortalStatusResponse getStatus();

    /**
     * 首次设置管理后台密码。
     *
     * @param request 密码设置请求。
     * @return 更新后的管理后台状态。
     */
    AdminPortalStatusResponse setPassword(AdminPortalPasswordRequest request);

    /**
     * 验证管理后台密码。
     *
     * @param password 待验证密码。
     */
    void verifyPassword(String password);

    /**
     * 查询每日英语配置。
     *
     * @param request 管理后台密码请求。
     * @return 每日英语配置。
     */
    DailyEnglishConfigResponse getEnglishConfig(AdminPortalPasswordRequest request);

    /**
     * 更新每日英语配置。
     *
     * @param request 每日英语配置更新请求。
     * @return 更新后的每日英语配置。
     */
    DailyEnglishConfigResponse updateEnglishConfig(DailyEnglishConfigRequest request);
}
