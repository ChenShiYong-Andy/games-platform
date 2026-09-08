package com.gamesplatform.system.admin.service.impl;

import com.gamesplatform.system.admin.service.AdminConfigService;
import com.gamesplatform.system.admin.service.AdminAuditService;
import com.gamesplatform.system.admin.service.AdminAuthorizationService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamesplatform.system.admin.dto.AdminPetGrowthAdjustRequest;
import com.gamesplatform.system.admin.dto.AdminPointsAdjustRequest;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.school.pet.entity.PetGrowthStageConfig;
import com.gamesplatform.school.pet.entity.PetUser;
import com.gamesplatform.school.pet.mapper.PetGrowthStageConfigMapper;
import com.gamesplatform.school.pet.mapper.PetUserMapper;
import com.gamesplatform.system.points.service.PointsService;
import com.gamesplatform.system.user.dto.UserProfileResponse;
import com.gamesplatform.system.user.entity.User;
import com.gamesplatform.system.user.mapper.UserMapper;
import com.gamesplatform.system.user.service.UserService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 管理后台中面向已关联普通用户的运维服务。
 */
@Service
@RequiredArgsConstructor
public class AdminConfigServiceImpl implements AdminConfigService {

    /**
     * 宠物等级上限。
     */
    private static final int MAX_PET_LEVEL = 70;

    /**
     * 用户数据访问组件。
     */
    @Schema(description = "用户数据访问组件")
    private final UserMapper userMapper;
    /**
     * 管理端关联授权服务。
     */
    @Schema(description = "管理端关联授权服务")
    private final AdminAuthorizationService authorizationService;
    /**
     * 管理员操作审计服务。
     */
    @Schema(description = "管理员操作审计服务")
    private final AdminAuditService auditService;
    /**
     * 用户服务。
     */
    @Schema(description = "用户服务")
    private final UserService userService;
    /**
     * 积分服务。
     */
    @Schema(description = "积分服务")
    private final PointsService pointsService;
    /**
     * 用户宠物数据访问组件。
     */
    @Schema(description = "用户宠物数据访问组件")
    private final PetUserMapper petUserMapper;
    /**
     * 宠物成长阶段配置数据访问组件。
     */
    @Schema(description = "宠物成长阶段配置数据访问组件")
    private final PetGrowthStageConfigMapper petGrowthStageConfigMapper;

    /**
     * 调整积分。
     *
     * @param adminId 管理员 ID。
     * @param userId 用户 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Transactional
    @Override
    public UserProfileResponse adjustPoints(Long adminId, Long userId, AdminPointsAdjustRequest request) {
        authorizationService.requireLinkedUser(adminId, userId);
        requireUser(userId);
        UserProfileResponse before = userService.getProfile(userId);
        Integer amount = request.getAmount();
        if (amount == 0) {
            throw new BusinessException("积分调整值不能为0");
        }
        String description = request.getDescription() == null || request.getDescription().isBlank()
                ? "管理员调整积分"
                : request.getDescription().trim();
        if (amount > 0) {
            pointsService.awardPoints(userId, amount, "ADMIN_ADJUST", adminId, description);
        } else {
            pointsService.deductPoints(userId, -amount, "ADMIN_ADJUST", adminId, description);
        }
        UserProfileResponse after = userService.getProfile(userId);
        auditService.record(adminId, userId, "POINTS", "ADJUST", before, after);
        return after;
    }

    /**
     * 扣减宠物成长值。
     *
     * @param adminId 管理员 ID。
     * @param userId 用户 ID。
     * @param request 请求参数。
     */
    @Transactional
    @Override
    public void deductPetGrowth(Long adminId, Long userId, AdminPetGrowthAdjustRequest request) {
        authorizationService.requireLinkedUser(adminId, userId);
        requireUser(userId);
        PetUser pet = petUserMapper.selectOne(
                new LambdaQueryWrapper<PetUser>()
                        .eq(PetUser::getUserId, userId)
                        .last("FOR UPDATE"));
        if (pet == null) {
            throw new BusinessException("请先领养宠物");
        }

        String before = "level=" + pet.getLevel() + ",exp=" + pet.getExp();

        int level = pet.getLevel() != null ? pet.getLevel() : 1;
        int exp = pet.getExp() != null ? pet.getExp() : 0;
        int totalExp = Math.max(0, (level - 1) * 100 + exp - request.getAmount());
        int newLevel = Math.min(MAX_PET_LEVEL, totalExp / 100 + 1);
        int newExp = newLevel >= MAX_PET_LEVEL ? 0 : totalExp % 100;
        int newStageNo = calculateStageNo(newLevel);

        pet.setLevel(newLevel);
        pet.setExp(newExp);
        pet.setStageNo(newStageNo);
        PetGrowthStageConfig stage = petGrowthStageConfigMapper.selectOne(
                new LambdaQueryWrapper<PetGrowthStageConfig>()
                        .eq(PetGrowthStageConfig::getPetType, pet.getPetType())
                        .eq(PetGrowthStageConfig::getColorCode, pet.getPetColorCode())
                        .eq(PetGrowthStageConfig::getStageNo, newStageNo)
                        .eq(PetGrowthStageConfig::getEnabled, 1)
                        .last("LIMIT 1"));
        if (stage != null) {
            pet.setPetAssetKey(stage.getAssetKey());
        }
        pet.setUpdateTime(LocalDateTime.now());
        petUserMapper.updateById(pet);
        String after = "level=" + pet.getLevel() + ",exp=" + pet.getExp();
        auditService.record(adminId, userId, "PET", "DEDUCT_GROWTH", before, after);
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId, userId)
                        .last("FOR UPDATE"));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private int calculateStageNo(int level) {
        if (level <= 5) {
            return 1;
        }
        if (level <= 15) {
            return 2;
        }
        if (level <= 30) {
            return 3;
        }
        if (level <= 50) {
            return 4;
        }
        return 5;
    }
}
