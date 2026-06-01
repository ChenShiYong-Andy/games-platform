package com.gamesplatform.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamesplatform.achievement.service.AchievementService;
import com.gamesplatform.auth.JwtTokenProvider;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.ranking.service.RankingService;
import com.gamesplatform.user.dto.*;
import com.gamesplatform.user.entity.User;
import com.gamesplatform.user.mapper.UserMapper;
import com.gamesplatform.zoo.entity.ZooCareRecord;
import com.gamesplatform.zoo.mapper.ZooCareRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户服务。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    /**
     * 默认每日数独次数上限。
     */
    private static final int DEFAULT_SUDOKU_DAILY_LIMIT = 5;
    /**
     * 默认每日动物园照料次数上限。
     */
    private static final int DEFAULT_ZOO_DAILY_CARE_LIMIT = 10;
    /**
     * 每日数独次数最大上限。
     */
    private static final int MAX_SUDOKU_DAILY_LIMIT = 100;
    /**
     * 每日动物园照料次数最大上限。
     */
    private static final int MAX_ZOO_DAILY_CARE_LIMIT = 1000;

    /**
     * 用户数据访问组件。
     */
    private final UserMapper userMapper;
    /**
     * 动物园照料记录数据访问组件。
     */
    private final ZooCareRecordMapper zooCareRecordMapper;
    /**
     * 密码编码器。
     */
    private final PasswordEncoder passwordEncoder;
    /**
     * JWT 令牌组件。
     */
    private final JwtTokenProvider jwtTokenProvider;
    /**
     * 成就服务。
     */
    private final AchievementService achievementService;
    /**
     * 排行榜服务。
     */
    private final RankingService rankingService;

    /**
     * 注册用户。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setLevel(1);
        user.setTotalPoints(0);
        user.setLoginStreak(0);
        user.setTotalClears(0);
        user.setSudokuDailyLimit(DEFAULT_SUDOKU_DAILY_LIMIT);
        user.setZooDailyCareLimit(DEFAULT_ZOO_DAILY_CARE_LIMIT);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.insert(user);

        rankingService.updateTotalPoints(user.getId(), 0);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        return AuthResponse.builder()
                .token(token)
                .user(toProfile(user))
                .build();
    }

    /**
     * 用户登录。
     *
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getUsername, request.getUsername()));
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException("用户名或密码错误");
        }

        updateLoginStreak(user);
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());
        return AuthResponse.builder()
                .token(token)
                .user(toProfile(user))
                .build();
    }

    /**
     * 查询用户资料。
     *
     * @param userId 用户 ID。
     * @return 处理结果。
     */
    public UserProfileResponse getProfile(Long userId) {
        User user = getUserById(userId);
        return toProfile(user);
    }

    /**
     * 更新用户资料。
     *
     * @param userId 用户 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = getUserById(userId);
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getSudokuDailyLimit() != null) {
            if (request.getSudokuDailyLimit() < 1 || request.getSudokuDailyLimit() > MAX_SUDOKU_DAILY_LIMIT) {
                throw new BusinessException("每日数独次数上限必须在 1 到 100 之间");
            }
            user.setSudokuDailyLimit(request.getSudokuDailyLimit());
        }
        if (request.getZooDailyCareLimit() != null) {
            if (request.getZooDailyCareLimit() < 1 || request.getZooDailyCareLimit() > MAX_ZOO_DAILY_CARE_LIMIT) {
                throw new BusinessException("每日动物园照顾次数上限必须在 1 到 1000 之间");
            }
            user.setZooDailyCareLimit(request.getZooDailyCareLimit());
        }
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return toProfile(user);
    }

    /**
     * 根据 ID 查询用户。
     *
     * @param userId 用户 ID。
     * @return 处理结果。
     */
    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    /**
     * 根据累计积分计算等级。
     *
     * @param totalPoints 累计积分。
     * @return 处理结果。
     */
    public int calculateLevel(int totalPoints) {
        if (totalPoints < 100) return 1;
        if (totalPoints < 500) return 2;
        if (totalPoints < 1500) return 3;
        if (totalPoints < 3000) return 4;
        if (totalPoints < 5000) return 5;
        if (totalPoints < 10000) return 6;
        return 7 + (totalPoints - 10000) / 5000;
    }

    private void updateLoginStreak(User user) {
        LocalDate today = LocalDate.now();
        LocalDate lastLogin = user.getLastLoginDate();
        int streak = user.getLoginStreak() != null ? user.getLoginStreak() : 0;

        if (lastLogin == null) {
            streak = 1;
        } else if (lastLogin.equals(today)) {
            // same day, no change
        } else if (lastLogin.equals(today.minusDays(1))) {
            streak++;
        } else {
            streak = 1;
        }

        user.setLoginStreak(streak);
        user.setLastLoginDate(today);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        achievementService.checkLoginStreak(user.getId(), streak);
    }

    private UserProfileResponse toProfile(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatarUrl(user.getAvatarUrl())
                .email(user.getEmail())
                .level(user.getLevel())
                .totalPoints(user.getTotalPoints())
                .loginStreak(user.getLoginStreak())
                .totalClears(user.getTotalClears())
                .totalZooCares(zooCareRecordMapper.selectCount(
                        new LambdaQueryWrapper<ZooCareRecord>()
                                .eq(ZooCareRecord::getUserId, user.getId())))
                .sudokuDailyLimit(user.getSudokuDailyLimit() != null
                        ? user.getSudokuDailyLimit()
                        : DEFAULT_SUDOKU_DAILY_LIMIT)
                .zooDailyCareLimit(user.getZooDailyCareLimit() != null
                        ? user.getZooDailyCareLimit()
                        : DEFAULT_ZOO_DAILY_CARE_LIMIT)
                .build();
    }
}
