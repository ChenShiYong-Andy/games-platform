package com.gamesplatform.system.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.system.admin.dto.AdminAuthResponse;
import com.gamesplatform.system.admin.dto.AdminLoginRequest;
import com.gamesplatform.system.admin.dto.AdminProfileResponse;
import com.gamesplatform.system.admin.dto.AdminRegisterRequest;
import com.gamesplatform.system.admin.entity.AdminAccount;
import com.gamesplatform.system.admin.mapper.AdminAccountMapper;
import com.gamesplatform.system.admin.security.AdminJwtTokenProvider;
import com.gamesplatform.system.admin.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 管理员独立认证服务实现。 */
@Service
@RequiredArgsConstructor
public class AdminAuthServiceImpl implements AdminAuthService {
    /** 管理员账户数据访问组件。 */
    @Schema(description = "管理员账户数据访问组件")
    private final AdminAccountMapper accountMapper;
    /** 密码编码器。 */
    @Schema(description = "密码编码器")
    private final PasswordEncoder passwordEncoder;
    /** 管理员令牌组件。 */
    @Schema(description = "管理员令牌组件")
    private final AdminJwtTokenProvider tokenProvider;

    /** {@inheritDoc} */
    @Override
    @Transactional
    public AdminAuthResponse register(AdminRegisterRequest request) {
        String username = request.getUsername().trim();
        if (accountMapper.selectCount(new LambdaQueryWrapper<AdminAccount>()
                .eq(AdminAccount::getUsername, username)) > 0) {
            throw new BusinessException("管理员账号已存在");
        }
        AdminAccount admin = new AdminAccount();
        admin.setUsername(username);
        admin.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        admin.setDisplayName(request.getDisplayName().trim());
        admin.setStatus("ENABLED");
        admin.setTokenVersion(0);
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        try {
            accountMapper.insert(admin);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("管理员账号已存在");
        }
        return response(admin);
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public AdminAuthResponse login(AdminLoginRequest request) {
        AdminAccount admin = accountMapper.selectOne(new LambdaQueryWrapper<AdminAccount>()
                .eq(AdminAccount::getUsername, request.getUsername().trim()));
        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new BusinessException("管理员账号或密码错误");
        }
        if (!"ENABLED".equals(admin.getStatus())) {
            throw new BusinessException("管理员账号已停用");
        }
        admin.setLastLoginAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        accountMapper.updateById(admin);
        return response(admin);
    }

    /** {@inheritDoc} */
    @Override
    public AdminProfileResponse getProfile(Long adminId) {
        AdminAccount admin = accountMapper.selectById(adminId);
        if (admin == null || !"ENABLED".equals(admin.getStatus())) {
            throw new BusinessException("管理员账号不可用");
        }
        return profile(admin);
    }

    private AdminAuthResponse response(AdminAccount admin) {
        return new AdminAuthResponse(tokenProvider.generateToken(admin), profile(admin));
    }

    private AdminProfileResponse profile(AdminAccount admin) {
        return new AdminProfileResponse(admin.getId(), admin.getUsername(), admin.getDisplayName());
    }
}
