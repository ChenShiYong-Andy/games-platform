package com.gamesplatform.system.admin.service.impl;

import com.gamesplatform.common.BusinessException;
import com.gamesplatform.school.english.dto.DailyEnglishConfigRequest;
import com.gamesplatform.school.english.dto.DailyEnglishConfigResponse;
import com.gamesplatform.school.english.entity.DailyEnglishConfig;
import com.gamesplatform.school.english.entity.DailyEnglishUserConfig;
import com.gamesplatform.school.english.mapper.DailyEnglishConfigMapper;
import com.gamesplatform.school.english.mapper.DailyEnglishUserConfigMapper;
import com.gamesplatform.system.admin.service.AdminAuditService;
import com.gamesplatform.system.admin.service.AdminAuthorizationService;
import com.gamesplatform.system.admin.service.AdminEnglishConfigService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 管理端用户每日英语配置服务实现。 */
@Service
@RequiredArgsConstructor
public class AdminEnglishConfigServiceImpl implements AdminEnglishConfigService {
    /** 系统默认配置固定主键。 */
    private static final long DEFAULT_CONFIG_ID = 1L;
    /** 管理端关联授权服务。 */
    @Schema(description = "管理端关联授权服务")
    private final AdminAuthorizationService authorizationService;
    /** 用户级每日英语配置数据访问组件。 */
    @Schema(description = "用户级每日英语配置数据访问组件")
    private final DailyEnglishUserConfigMapper userConfigMapper;
    /** 系统默认每日英语配置数据访问组件。 */
    @Schema(description = "系统默认每日英语配置数据访问组件")
    private final DailyEnglishConfigMapper defaultConfigMapper;
    /** 管理员操作审计服务。 */
    @Schema(description = "管理员操作审计服务")
    private final AdminAuditService auditService;

    /** {@inheritDoc} */
    @Override
    public DailyEnglishConfigResponse getConfig(Long adminId, Long userId) {
        authorizationService.requireLinkedUser(adminId, userId);
        DailyEnglishUserConfig config = userConfigMapper.selectById(userId);
        return config == null ? defaultConfig() : response(config);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "dailyEnglishPractice", allEntries = true)
    public DailyEnglishConfigResponse updateConfig(
            Long adminId, Long userId, DailyEnglishConfigRequest request) {
        authorizationService.requireLinkedUser(adminId, userId);
        DailyEnglishConfigResponse before = getConfig(adminId, userId);
        DailyEnglishUserConfig config = userConfigMapper.selectById(userId);
        LocalDateTime now = LocalDateTime.now();
        if (config == null) {
            config = new DailyEnglishUserConfig();
            config.setUserId(userId);
            config.setVersion(0);
            config.setCreatedAt(now);
            config.setGradeLevel(request.getGradeLevel());
            config.setSkillMarkdown(trim(request.getSkillMarkdown()));
            config.setUpdatedByAdminId(adminId);
            config.setUpdatedAt(now);
            userConfigMapper.insert(config);
        } else {
            config.setGradeLevel(request.getGradeLevel());
            config.setSkillMarkdown(trim(request.getSkillMarkdown()));
            config.setUpdatedByAdminId(adminId);
            config.setVersion(config.getVersion() + 1);
            config.setUpdatedAt(now);
            userConfigMapper.updateById(config);
        }
        DailyEnglishConfigResponse after = response(config);
        auditService.record(adminId, userId, "DAILY_ENGLISH", "UPDATE_CONFIG", before, after);
        return after;
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    @CacheEvict(cacheNames = "dailyEnglishPractice", allEntries = true)
    public DailyEnglishConfigResponse resetConfig(Long adminId, Long userId) {
        authorizationService.requireLinkedUser(adminId, userId);
        DailyEnglishConfigResponse before = getConfig(adminId, userId);
        userConfigMapper.deleteById(userId);
        DailyEnglishConfigResponse after = defaultConfig();
        auditService.record(adminId, userId, "DAILY_ENGLISH", "RESET_CONFIG", before, after);
        return after;
    }

    private DailyEnglishConfigResponse defaultConfig() {
        DailyEnglishConfig config = defaultConfigMapper.selectById(DEFAULT_CONFIG_ID);
        if (config == null) {
            throw new BusinessException("系统每日英语默认配置不存在");
        }
        return new DailyEnglishConfigResponse(config.getGradeLevel(), config.getSkillMarkdown());
    }

    private DailyEnglishConfigResponse response(DailyEnglishUserConfig config) {
        return new DailyEnglishConfigResponse(config.getGradeLevel(), config.getSkillMarkdown());
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
