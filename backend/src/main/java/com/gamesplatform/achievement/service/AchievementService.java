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
import java.util.stream.Collectors;

/**
 * 成就服务。
 */
@Service
@RequiredArgsConstructor
public class AchievementService {

    /**
     * 平台级成就编码。
     */
    public static final String GAME_PLATFORM = "PLATFORM";
    /**
     * 数独游戏编码。
     */
    public static final String GAME_SUDOKU = "SUDOKU";
    /**
     * 成就数据访问组件。
     */
    private final AchievementMapper achievementMapper;
    /**
     * 用户成就数据访问组件。
     */
    private final UserAchievementMapper userAchievementMapper;

    /**
     * 日期时间格式化器。
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 查询用户成就。
     *
     * @param userId 用户 ID。
     * @param gameCode 游戏编码，为空时查询全部成就。
     * @return 处理结果。
     */
    public List<AchievementResponse> getUserAchievements(Long userId, String gameCode) {
        LambdaQueryWrapper<Achievement> achievementQuery = new LambdaQueryWrapper<>();
        if (gameCode != null && !gameCode.isBlank()) {
            achievementQuery.eq(Achievement::getGameCode, gameCode.toUpperCase());
        }
        achievementQuery.ne(Achievement::getGameCode, "ZOO");
        achievementQuery.orderByAsc(Achievement::getGameCode).orderByAsc(Achievement::getId);
        List<Achievement> all = achievementMapper.selectList(achievementQuery);
        List<UserAchievement> unlocked = userAchievementMapper.selectList(
                new LambdaQueryWrapper<UserAchievement>().eq(UserAchievement::getUserId, userId));
        Map<Long, UserAchievement> unlockedMap = unlocked.stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, ua -> ua));

        return all.stream().map(a -> {
            UserAchievement ua = unlockedMap.get(a.getId());
            return AchievementResponse.builder()
                    .id(a.getId())
                    .code(a.getCode())
                    .gameCode(a.getGameCode())
                    .name(a.getName())
                    .description(a.getDescription())
                    .icon(a.getIcon())
                    .unlocked(ua != null)
                    .unlockedAt(ua != null ? ua.getUnlockedAt().format(FORMATTER) : null)
                    .build();
        }).toList();
    }

    /**
     * 解锁用户成就。
     *
     * @param userId 用户 ID。
     * @param gameCode 游戏编码。
     * @param code 业务编码。
     */
    @Transactional
    public void unlockAchievement(Long userId, String gameCode, String code) {
        Achievement achievement = achievementMapper.selectOne(
                new LambdaQueryWrapper<Achievement>()
                        .eq(Achievement::getGameCode, gameCode)
                        .eq(Achievement::getCode, code));
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

    /**
     * 检查连续登录成就。
     *
     * @param userId 用户 ID。
     * @param streak 连续登录天数。
     */
    public void checkLoginStreak(Long userId, int streak) {
        if (streak >= 3) unlockAchievement(userId, GAME_PLATFORM, "LOGIN_STREAK_3");
        if (streak >= 7) unlockAchievement(userId, GAME_PLATFORM, "LOGIN_STREAK_7");
    }

    /**
     * 检查游戏通关成就。
     *
     * @param userId 用户 ID。
     * @param gameCode 游戏编码。
     * @param difficulty 游戏难度。
     * @param totalClears 累计通关次数。
     */
    public void checkGameComplete(Long userId, String gameCode, String difficulty, long totalClears) {
        if (totalClears == 1) unlockAchievement(userId, gameCode, "FIRST_CLEAR");
        if (totalClears >= 10) unlockAchievement(userId, gameCode, "CLEAR_10");
        if (totalClears >= 50) unlockAchievement(userId, gameCode, "CLEAR_50");
        if (totalClears >= 100) unlockAchievement(userId, gameCode, "CLEAR_100");
        if ("HARD".equalsIgnoreCase(difficulty)) unlockAchievement(userId, gameCode, "HARD_CLEAR");
    }

}
