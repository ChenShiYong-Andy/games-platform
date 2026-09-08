package com.gamesplatform.system.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.system.admin.entity.AdminUserRelation;
import com.gamesplatform.system.admin.mapper.AdminUserRelationMapper;
import com.gamesplatform.system.admin.service.AdminAuthorizationService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.stereotype.Service;

/** 管理端关联用户访问授权服务实现。 */
@Service
@RequiredArgsConstructor
public class AdminAuthorizationServiceImpl implements AdminAuthorizationService {
    /** 管理员用户关联数据访问组件。 */
    @Schema(description = "管理员用户关联数据访问组件")
    private final AdminUserRelationMapper relationMapper;

    /** {@inheritDoc} */
    @Override
    public void requireLinkedUser(Long adminId, Long userId) {
        long count = relationMapper.selectCount(new LambdaQueryWrapper<AdminUserRelation>()
                .eq(AdminUserRelation::getAdminId, adminId)
                .eq(AdminUserRelation::getUserId, userId));
        if (count == 0) {
            throw new BusinessException(403, "该用户未关联到当前管理员");
        }
    }
}
