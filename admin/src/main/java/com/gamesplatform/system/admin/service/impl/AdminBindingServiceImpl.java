package com.gamesplatform.system.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.system.admin.dto.AdminBindUserRequest;
import com.gamesplatform.system.admin.dto.AdminBindingCodeResponse;
import com.gamesplatform.system.admin.dto.AdminBindingStatusResponse;
import com.gamesplatform.system.admin.dto.ManagedUserResponse;
import com.gamesplatform.system.admin.entity.AdminAccount;
import com.gamesplatform.system.admin.entity.AdminUserRelation;
import com.gamesplatform.system.admin.mapper.AdminAccountMapper;
import com.gamesplatform.system.admin.mapper.AdminUserRelationMapper;
import com.gamesplatform.system.admin.service.AdminAuditService;
import com.gamesplatform.system.admin.service.AdminBindingService;
import com.gamesplatform.system.user.entity.User;
import com.gamesplatform.system.user.mapper.UserMapper;
import com.gamesplatform.system.user.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.List;

/** 管理员与普通用户关联服务实现。 */
@Service
public class AdminBindingServiceImpl implements AdminBindingService {
    /** 绑定码字符表。 */
    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    /** 绑定码长度。 */
    private static final int CODE_LENGTH = 8;
    /** 用户当前绑定码 Redis 键前缀。 */
    private static final String USER_CODE_KEY_PREFIX = "admin-binding:user:";
    /** 一次性绑定码 Redis 键前缀。 */
    private static final String CODE_KEY_PREFIX = "admin-binding:code:";

    /** 管理员用户关联数据访问组件。 */
    @Schema(description = "管理员用户关联数据访问组件")
    private final AdminUserRelationMapper relationMapper;
    /** 管理员账户数据访问组件。 */
    @Schema(description = "管理员账户数据访问组件")
    private final AdminAccountMapper accountMapper;
    /** 用户数据访问组件。 */
    @Schema(description = "用户数据访问组件")
    private final UserMapper userMapper;
    /** 用户业务服务。 */
    @Schema(description = "用户业务服务")
    private final UserService userService;
    /** Redis 字符串访问组件。 */
    @Schema(description = "Redis 字符串访问组件")
    private final StringRedisTemplate redisTemplate;
    /** 管理员审计服务。 */
    @Schema(description = "管理员审计服务")
    private final AdminAuditService auditService;
    /** 绑定码有效时长。 */
    @Schema(description = "绑定码有效时长")
    private final Duration codeTtl;
    /** 安全随机数生成器。 */
    @Schema(description = "安全随机数生成器")
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 创建关联服务。
     * @param relationMapper 关联数据访问组件。
     * @param accountMapper 管理员账户数据访问组件。
     * @param userMapper 用户数据访问组件。
     * @param userService 用户业务服务。
     * @param redisTemplate Redis 访问组件。
     * @param auditService 审计服务。
     * @param ttlMinutes 绑定码有效分钟数。
     */
    public AdminBindingServiceImpl(
            AdminUserRelationMapper relationMapper,
            AdminAccountMapper accountMapper,
            UserMapper userMapper,
            UserService userService,
            StringRedisTemplate redisTemplate,
            AdminAuditService auditService,
            @Value("${admin.binding-code.ttl-minutes:10}") long ttlMinutes) {
        this.relationMapper = relationMapper;
        this.accountMapper = accountMapper;
        this.userMapper = userMapper;
        this.userService = userService;
        this.redisTemplate = redisTemplate;
        this.auditService = auditService;
        this.codeTtl = Duration.ofMinutes(Math.max(1, ttlMinutes));
    }

    /** {@inheritDoc} */
    @Override
    public AdminBindingCodeResponse generateBindingCode(Long userId) {
        if (findRelationByUser(userId) != null) {
            throw new BusinessException("当前账号已经关联管理员");
        }
        User user = userService.getUserById(userId);
        String userKey = USER_CODE_KEY_PREFIX + userId;
        String oldCode = redisTemplate.opsForValue().get(userKey);
        if (oldCode != null) {
            redisTemplate.delete(codeKey(user.getUsername(), oldCode));
        }
        String code = randomCode();
        redisTemplate.opsForValue().set(userKey, code, codeTtl);
        redisTemplate.opsForValue().set(codeKey(user.getUsername(), code), String.valueOf(userId), codeTtl);
        return new AdminBindingCodeResponse(code, LocalDateTime.now().plus(codeTtl));
    }

    /** {@inheritDoc} */
    @Override
    public AdminBindingStatusResponse getBindingStatus(Long userId) {
        AdminUserRelation relation = findRelationByUser(userId);
        if (relation == null) {
            return new AdminBindingStatusResponse(false, null, null);
        }
        AdminAccount admin = accountMapper.selectById(relation.getAdminId());
        return new AdminBindingStatusResponse(
                true,
                admin == null ? null : admin.getUsername(),
                admin == null ? null : admin.getDisplayName());
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public ManagedUserResponse bindUser(Long adminId, AdminBindUserRequest request) {
        String username = request.getUsername().trim();
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        String suppliedCode = request.getBindingCode().trim().toUpperCase();
        String cachedUserId = redisTemplate.opsForValue().getAndDelete(codeKey(username, suppliedCode));
        if (!String.valueOf(user.getId()).equals(cachedUserId)) {
            throw new BusinessException("绑定码无效或已过期");
        }
        redisTemplate.delete(USER_CODE_KEY_PREFIX + user.getId());
        AdminUserRelation relation = new AdminUserRelation();
        relation.setAdminId(adminId);
        relation.setUserId(user.getId());
        relation.setBoundAt(LocalDateTime.now());
        try {
            relationMapper.insert(relation);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException("该用户已经关联其他管理员");
        }
        ManagedUserResponse response = managedUser(relation);
        auditService.record(adminId, user.getId(), "USER_RELATION", "BIND", null, response);
        return response;
    }

    /** {@inheritDoc} */
    @Override
    public List<ManagedUserResponse> listManagedUsers(Long adminId) {
        return relationMapper.selectList(new LambdaQueryWrapper<AdminUserRelation>()
                        .eq(AdminUserRelation::getAdminId, adminId)
                        .orderByDesc(AdminUserRelation::getBoundAt))
                .stream().map(this::managedUser).toList();
    }

    /** {@inheritDoc} */
    @Override
    @Transactional
    public void unbindUser(Long adminId, Long userId) {
        AdminUserRelation relation = relationMapper.selectOne(new LambdaQueryWrapper<AdminUserRelation>()
                .eq(AdminUserRelation::getAdminId, adminId)
                .eq(AdminUserRelation::getUserId, userId));
        if (relation == null) {
            throw new BusinessException("该用户未关联到当前管理员");
        }
        relationMapper.deleteById(relation.getId());
        auditService.record(adminId, userId, "USER_RELATION", "UNBIND", relation, null);
    }

    private AdminUserRelation findRelationByUser(Long userId) {
        return relationMapper.selectOne(new LambdaQueryWrapper<AdminUserRelation>()
                .eq(AdminUserRelation::getUserId, userId));
    }

    private ManagedUserResponse managedUser(AdminUserRelation relation) {
        return new ManagedUserResponse(userService.getProfile(relation.getUserId()), relation.getBoundAt());
    }

    private String randomCode() {
        StringBuilder code = new StringBuilder(CODE_LENGTH);
        for (int index = 0; index < CODE_LENGTH; index++) {
            code.append(CODE_CHARS.charAt(secureRandom.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }

    private String codeKey(String username, String code) {
        return CODE_KEY_PREFIX + username.trim().toLowerCase() + ":" + sha256(code);
    }

    private String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("当前运行环境不支持 SHA-256", exception);
        }
    }
}
