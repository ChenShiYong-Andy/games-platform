package com.gamesplatform.achievement.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamesplatform.achievement.dto.AchievementResponse;
import com.gamesplatform.achievement.entity.Achievement;
import com.gamesplatform.achievement.entity.UserAchievement;
import com.gamesplatform.achievement.mapper.AchievementMapper;
import com.gamesplatform.achievement.mapper.UserAchievementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public List<AchievementResponse> getUserAchievements(Long userId) {
        List<Achievement> all = achievementMapper.selectList(null);
        List<UserAchievement> unlocked = userAchievementMapper.selectList(
                new LambdaQueryWrapper<UserAchievement>().eq(UserAchievement::getUserId, userId));
        Map<Long, UserAchievement> unlockedMap = unlocked.stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, ua -> ua));

        return all.stream().map(a -> {
            UserAchievement ua = unlockedMap.get(a.getId());
            return AchievementResponse.builder()
                    .id(a.getId())
                    .code(a.getCode())
                    .name(a.getName())
                    .description(a.getDescription())
                    .icon(a.getIcon())
                    .unlocked(ua != null)
                    .unlockedAt(ua != null ? ua.getUnlockedAt().format(FORMATTER) : null)
                    .build();
        }).toList();
    }

    @Transactional
    public void unlockAchievement(Long userId, String code) {
        Achievement achievement = achievementMapper.selectOne(
                new LambdaQueryWrapper<Achievement>().eq(Achievement::getCode, code));
        if (achievement == null) return;

        Long count = userAchievementMapper.selectCount(
                new LambdaQueryWrapper<UserAchievement>()
                        .eq(UserAchievement::getUserId, userId)
                        .eq(UserAchievement::getAchievementId, achievement.getId()));
        if (count > 0) return;

        UserAchievement ua = new UserAchievement();
        ua.setUserId(userId);
        ua.setAchievementId(achievement.getId());
        ua.setUnlockedAt(LocalDateTime.now());
        userAchievementMapper.insert(ua);
    }

    public void checkLoginStreak(Long userId, int streak) {
        if (streak >= 3) unlockAchievement(userId, "LOGIN_STREAK_3");
        if (streak >= 7) unlockAchievement(userId, "LOGIN_STREAK_7");
    }

    public void checkGameComplete(Long userId, String difficulty, int totalClears) {
        if (totalClears == 1) unlockAchievement(userId, "FIRST_CLEAR");
        if (totalClears >= 10) unlockAchievement(userId, "CLEAR_10");
        if (totalClears >= 50) unlockAchievement(userId, "CLEAR_50");
        if (totalClears >= 100) unlockAchievement(userId, "CLEAR_100");
        if ("HARD".equalsIgnoreCase(difficulty)) unlockAchievement(userId, "HARD_CLEAR");
    }
}
