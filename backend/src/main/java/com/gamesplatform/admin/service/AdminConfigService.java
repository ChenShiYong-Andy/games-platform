package com.gamesplatform.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamesplatform.admin.dto.AdminConfigStatusResponse;
import com.gamesplatform.admin.dto.AdminGameConfigRequest;
import com.gamesplatform.admin.dto.AdminPointsAdjustRequest;
import com.gamesplatform.admin.dto.SetAdminPasswordRequest;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.points.service.PointsService;
import com.gamesplatform.user.dto.UserProfileResponse;
import com.gamesplatform.user.entity.User;
import com.gamesplatform.user.mapper.UserMapper;
import com.gamesplatform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 管理员配置服务。
 */
@Service
@RequiredArgsConstructor
public class AdminConfigService {

    /**
     * 默认每日数独次数最大上限。
     */
    private static final int MAX_SUDOKU_DAILY_LIMIT = 100;

    /**
     * 用户数据访问组件。
     */
    private final UserMapper userMapper;
    /**
     * 密码编码器。
     */
    private final PasswordEncoder passwordEncoder;
    /**
     * 用户服务。
     */
    private final UserService userService;
    /**
     * 积分服务。
     */
    private final PointsService pointsService;

    /**
     * 查询管理配置状态。
     *
     * @param userId 用户 ID。
     * @return 处理结果。
     */
    public AdminConfigStatusResponse getStatus(Long userId) {
        User user = userService.getUserById(userId);
        return AdminConfigStatusResponse.builder()
                .adminPasswordSet(hasAdminPassword(user))
                .build();
    }

    /**
     * 设置管理密码。
     *
     * @param userId 用户 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Transactional
    public AdminConfigStatusResponse setPassword(Long userId, SetAdminPasswordRequest request) {
        String password = request.getPassword().trim();
        if (password.length() < 6) {
            throw new BusinessException("管理密码不能少于6位");
        }
        User user = userService.getUserById(userId);
        if (hasAdminPassword(user)) {
            throw new BusinessException("管理密码已设置");
        }
        user.setAdminPasswordHash(passwordEncoder.encode(password));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return AdminConfigStatusResponse.builder().adminPasswordSet(true).build();
    }

    /**
     * 更新游戏配置。
     *
     * @param userId 用户 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Transactional
    public UserProfileResponse updateGameConfig(Long userId, AdminGameConfigRequest request) {
        User user = requireAdmin(userId, request.getAdminPassword());
        Integer sudokuDailyLimit = request.getSudokuDailyLimit();
        if (sudokuDailyLimit < 1 || sudokuDailyLimit > MAX_SUDOKU_DAILY_LIMIT) {
            throw new BusinessException("每日数独次数上限必须在 1 到 100 之间");
        }
        user.setSudokuDailyLimit(sudokuDailyLimit);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return userService.getProfile(userId);
    }

    /**
     * 调整积分。
     *
     * @param userId 用户 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Transactional
    public UserProfileResponse adjustPoints(Long userId, AdminPointsAdjustRequest request) {
        requireAdmin(userId, request.getAdminPassword());
        Integer amount = request.getAmount();
        if (amount == 0) {
            throw new BusinessException("积分调整值不能为0");
        }
        String description = request.getDescription() == null || request.getDescription().isBlank()
                ? "管理员调整积分"
                : request.getDescription().trim();
        if (amount > 0) {
            pointsService.awardPoints(userId, amount, "ADMIN_ADJUST", userId, description);
        } else {
            pointsService.deductPoints(userId, -amount, "ADMIN_ADJUST", userId, description);
        }
        return userService.getProfile(userId);
    }

    private User requireAdmin(Long userId, String adminPassword) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId, userId)
                        .last("FOR UPDATE"));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (!hasAdminPassword(user)) {
            throw new BusinessException("请先设置管理密码");
        }
        if (!passwordEncoder.matches(adminPassword, user.getAdminPasswordHash())) {
            throw new BusinessException("管理密码错误");
        }
        return user;
    }

    private boolean hasAdminPassword(User user) {
        return user.getAdminPasswordHash() != null && !user.getAdminPasswordHash().isBlank();
    }
}
