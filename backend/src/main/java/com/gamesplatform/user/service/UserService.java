package com.gamesplatform.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamesplatform.achievement.service.AchievementService;
import com.gamesplatform.auth.JwtTokenProvider;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.ranking.service.RankingService;
import com.gamesplatform.user.dto.*;
import com.gamesplatform.user.entity.User;
import com.gamesplatform.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AchievementService achievementService;
    private final RankingService rankingService;

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

    public UserProfileResponse getProfile(Long userId) {
        User user = getUserById(userId);
        return toProfile(user);
    }

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
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        return toProfile(user);
    }

    public User getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

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
                .build();
    }
}
