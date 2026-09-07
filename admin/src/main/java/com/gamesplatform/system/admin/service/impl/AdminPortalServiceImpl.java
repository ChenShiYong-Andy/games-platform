package com.gamesplatform.system.admin.service.impl;

import com.gamesplatform.system.admin.service.AdminPortalService;

import com.gamesplatform.system.admin.dto.AdminPortalPasswordRequest;
import com.gamesplatform.system.admin.dto.AdminPortalStatusResponse;
import com.gamesplatform.system.admin.entity.AdminPortalConfig;
import com.gamesplatform.system.admin.mapper.AdminPortalConfigMapper;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.school.english.dto.DailyEnglishConfigRequest;
import com.gamesplatform.school.english.dto.DailyEnglishConfigResponse;
import com.gamesplatform.school.english.entity.DailyEnglishConfig;
import com.gamesplatform.school.english.mapper.DailyEnglishConfigMapper;
import com.gamesplatform.school.english.mapper.DailyEnglishPracticeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 独立管理后台服务。
 */
@Service
@RequiredArgsConstructor
public class AdminPortalServiceImpl implements AdminPortalService {

    private static final long CONFIG_ID = 1L;

    private final AdminPortalConfigMapper portalConfigMapper;
    private final DailyEnglishConfigMapper englishConfigMapper;
    private final DailyEnglishPracticeMapper practiceMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public AdminPortalStatusResponse getStatus() {
        return new AdminPortalStatusResponse(portalConfigMapper.selectById(CONFIG_ID) != null);
    }

    @Transactional
    @Override
    public AdminPortalStatusResponse setPassword(AdminPortalPasswordRequest request) {
        if (portalConfigMapper.selectById(CONFIG_ID) != null) {
            throw new BusinessException("管理后台密码已设置");
        }
        AdminPortalConfig config = new AdminPortalConfig();
        config.setId(CONFIG_ID);
        config.setPasswordHash(passwordEncoder.encode(request.getPassword().trim()));
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        portalConfigMapper.insert(config);
        return new AdminPortalStatusResponse(true);
    }

    @Override
    public void verifyPassword(String password) {
        AdminPortalConfig config = portalConfigMapper.selectById(CONFIG_ID);
        if (config == null) {
            throw new BusinessException("请先设置管理后台密码");
        }
        if (password == null || !passwordEncoder.matches(password, config.getPasswordHash())) {
            throw new BusinessException("管理后台密码错误");
        }
    }

    @Override
    public DailyEnglishConfigResponse getEnglishConfig(AdminPortalPasswordRequest request) {
        verifyPassword(request.getPassword());
        DailyEnglishConfig config = requireEnglishConfig();
        return new DailyEnglishConfigResponse(config.getGradeLevel(), config.getSkillMarkdown());
    }

    @Transactional
    @CacheEvict(cacheNames = "daily-english:practice", allEntries = true)
    @Override
    public DailyEnglishConfigResponse updateEnglishConfig(DailyEnglishConfigRequest request) {
        verifyPassword(request.getAdminPassword());
        DailyEnglishConfig config = requireEnglishConfig();
        config.setGradeLevel(request.getGradeLevel());
        config.setSkillMarkdown(request.getSkillMarkdown() == null ? "" : request.getSkillMarkdown().trim());
        config.setUpdatedAt(LocalDateTime.now());
        englishConfigMapper.updateById(config);
        practiceMapper.delete(null);
        return new DailyEnglishConfigResponse(config.getGradeLevel(), config.getSkillMarkdown());
    }

    private DailyEnglishConfig requireEnglishConfig() {
        DailyEnglishConfig config = englishConfigMapper.selectById(CONFIG_ID);
        if (config == null) {
            throw new BusinessException("每日英语配置不存在，请执行数据库迁移");
        }
        return config;
    }
}
