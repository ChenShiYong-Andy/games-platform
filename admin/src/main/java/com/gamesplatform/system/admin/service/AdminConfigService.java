package com.gamesplatform.system.admin.service;

import com.gamesplatform.system.admin.dto.AdminPetGrowthAdjustRequest;
import com.gamesplatform.system.admin.dto.AdminPointsAdjustRequest;
import com.gamesplatform.system.user.dto.UserProfileResponse;

/**
 * 管理端用户数据配置服务。
 */
public interface AdminConfigService {

    /**
     * 调整指定用户积分。
     *
     * @param adminId 管理员 ID。
     * @param userId 用户 ID。
     * @param request 积分调整请求。
     * @return 更新后的用户资料。
     */
    UserProfileResponse adjustPoints(Long adminId, Long userId, AdminPointsAdjustRequest request);

    /**
     * 扣减指定用户的宠物成长值。
     *
     * @param adminId 管理员 ID。
     * @param userId 用户 ID。
     * @param request 宠物成长值调整请求。
     */
    void deductPetGrowth(Long adminId, Long userId, AdminPetGrowthAdjustRequest request);
}
