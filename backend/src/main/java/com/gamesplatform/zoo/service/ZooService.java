package com.gamesplatform.zoo.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamesplatform.achievement.service.AchievementService;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.points.service.PointsService;
import com.gamesplatform.ranking.service.RankingService;
import com.gamesplatform.user.entity.User;
import com.gamesplatform.user.mapper.UserMapper;
import com.gamesplatform.zoo.dto.ZooCareResponse;
import com.gamesplatform.zoo.entity.ZooCareRecord;
import com.gamesplatform.zoo.entity.ZooGame;
import com.gamesplatform.zoo.mapper.ZooCareRecordMapper;
import com.gamesplatform.zoo.mapper.ZooGameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 动物园游戏服务。
 */
@Service
@RequiredArgsConstructor
public class ZooService {

    /**
     * 默认每日动物园照料次数上限。
     */
    private static final int DEFAULT_ZOO_DAILY_CARE_LIMIT = 3;
    /**
     * 动物园游戏目标分数。
     */
    private static final int TARGET_SCORE = 10;
    /**
     * 完成一局动物园游戏的奖励积分。
     */
    private static final int COMPLETION_REWARD_POINTS = 10;
    /**
     * 动物园游戏时长秒数。
     */
    private static final int GAME_SECONDS = 120;
    /**
     * 动物园照料需求列表。
     */
    private static final List<String> NEEDS = List.of("food", "water", "ball");

    /**
     * 用户数据访问组件。
     */
    private final UserMapper userMapper;
    /**
     * 动物园照料记录数据访问组件。
     */
    private final ZooCareRecordMapper zooCareRecordMapper;
    /**
     * 动物园游戏记录数据访问组件。
     */
    private final ZooGameMapper zooGameMapper;
    /**
     * 积分服务。
     */
    private final PointsService pointsService;
    /**
     * 成就服务。
     */
    private final AchievementService achievementService;
    /**
     * 排行榜服务。
     */
    private final RankingService rankingService;

    /**
     * 开始动物园游戏。
     *
     * @param userId 用户 ID。
     * @return 处理结果。
     */
    @Transactional
    public ZooCareResponse startGame(Long userId) {
        User user = getUser(userId);
        ZooGame activeGame = zooGameMapper.selectOne(
                new LambdaQueryWrapper<ZooGame>()
                        .eq(ZooGame::getUserId, userId)
                        .eq(ZooGame::getStatus, "IN_PROGRESS")
                        .orderByDesc(ZooGame::getStartedAt)
                        .last("LIMIT 1 FOR UPDATE"));
        if (activeGame != null && getRemainingSeconds(activeGame) > 0) {
            return toResponse(activeGame, user, null, "继续上次的照顾挑战");
        }
        if (activeGame != null) {
            settleFailedGame(activeGame, user);
        }

        ZooGame game = new ZooGame();
        game.setUserId(userId);
        game.setStatus("IN_PROGRESS");
        game.setCurrentNeed(randomNeed(null));
        game.setScore(0);
        game.setPointsAwarded(0);
        game.setPointsDeducted(0);
        game.setStartedAt(LocalDateTime.now());
        zooGameMapper.insert(game);
        return toResponse(game, user, null, "挑战开始");
    }

    /**
     * 记录动物园照料操作。
     *
     * @param userId 用户 ID。
     * @param gameId 游戏 ID。
     * @param tool 照料工具。
     * @return 处理结果。
     */
    @Transactional
    public ZooCareResponse recordCare(Long userId, Long gameId, String tool) {
        if (!Set.copyOf(NEEDS).contains(tool)) {
            throw new BusinessException("无效的照顾工具");
        }
        User user = getUser(userId);
        ZooGame game = getGame(userId, gameId);
        if (!"IN_PROGRESS".equals(game.getStatus())) {
            return toResponse(game, user, null, "本局游戏已结束");
        }
        if (getRemainingSeconds(game) <= 0) {
            settleFailedGame(game, user);
            return toResponse(game, user, null, "时间到了，挑战失败");
        }

        if (!game.getCurrentNeed().equals(tool)) {
            game.setScore(Math.max(0, game.getScore() - 1));
            zooGameMapper.updateById(game);
            return toResponse(game, user, false, "这个工具不对哦，爱心分数 -1");
        }

        int remainingToday = recordSuccessfulCare(user);
        game.setScore(game.getScore() + 1);
        if (game.getScore() >= TARGET_SCORE) {
            game.setStatus("COMPLETED");
            int totalPoints = pointsService.awardPoints(user.getId(), COMPLETION_REWARD_POINTS,
                    "ZOO_COMPLETE", game.getId(), "完成动物园照顾挑战");
            game.setPointsAwarded(COMPLETION_REWARD_POINTS);
            user.setTotalPoints(totalPoints);
            game.setCompletedAt(LocalDateTime.now());
        } else {
            game.setCurrentNeed(randomNeed(game.getCurrentNeed()));
        }
        zooGameMapper.updateById(game);
        if ("COMPLETED".equals(game.getStatus())) {
            int elapsedSeconds = (int) Duration.between(game.getStartedAt(), game.getCompletedAt()).getSeconds();
            rankingService.updateZooAverageDuration(userId, elapsedSeconds);
        }
        Long totalCares = zooCareRecordMapper.selectCount(
                new LambdaQueryWrapper<ZooCareRecord>().eq(ZooCareRecord::getUserId, userId));
        achievementService.checkZooCare(userId, totalCares);
        return toResponse(game, user, true, "谢谢你，爱心分数 +1", remainingToday);
    }

    /**
     * 结算动物园游戏。
     *
     * @param userId 用户 ID。
     * @param gameId 游戏 ID。
     * @return 处理结果。
     */
    @Transactional
    public ZooCareResponse settleGame(Long userId, Long gameId) {
        User user = getUser(userId);
        ZooGame game = getGame(userId, gameId);
        if ("IN_PROGRESS".equals(game.getStatus()) && getRemainingSeconds(game) <= 0) {
            settleFailedGame(game, user);
        }
        return toResponse(game, user, null,
                "FAILED".equals(game.getStatus()) ? "时间到了，挑战失败" : "挑战仍在进行");
    }

    private int recordSuccessfulCare(User user) {
        int dailyLimit = user.getZooDailyCareLimit() != null
                ? user.getZooDailyCareLimit()
                : DEFAULT_ZOO_DAILY_CARE_LIMIT;
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        Long careCount = zooCareRecordMapper.selectCount(
                new LambdaQueryWrapper<ZooCareRecord>()
                        .eq(ZooCareRecord::getUserId, user.getId())
                        .ge(ZooCareRecord::getCaredAt, startOfDay)
                        .lt(ZooCareRecord::getCaredAt, startOfDay.plusDays(1)));
        if (careCount >= dailyLimit) {
            throw new BusinessException("今天的动物园照顾次数已用完，请明天再来");
        }

        ZooCareRecord record = new ZooCareRecord();
        record.setUserId(user.getId());
        record.setCaredAt(LocalDateTime.now());
        zooCareRecordMapper.insert(record);
        return dailyLimit - careCount.intValue() - 1;
    }

    private void settleFailedGame(ZooGame game, User user) {
        int missingScore = TARGET_SCORE - game.getScore();
        int beforePoints = user.getTotalPoints();
        int totalPoints = pointsService.deductPoints(user.getId(), missingScore,
                "ZOO_TIMEOUT", game.getId(), "动物园挑战超时，扣除未完成爱心分数");
        game.setStatus("FAILED");
        game.setPointsDeducted(beforePoints - totalPoints);
        game.setCompletedAt(LocalDateTime.now());
        zooGameMapper.updateById(game);
        user.setTotalPoints(totalPoints);
    }

    private ZooGame getGame(Long userId, Long gameId) {
        ZooGame game = zooGameMapper.selectOne(
                new LambdaQueryWrapper<ZooGame>()
                        .eq(ZooGame::getId, gameId)
                        .eq(ZooGame::getUserId, userId)
                        .last("FOR UPDATE"));
        if (game == null) {
            throw new BusinessException("动物园游戏不存在");
        }
        return game;
    }

    private User getUser(Long userId) {
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId, userId)
                        .last("FOR UPDATE"));
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private int getRemainingSeconds(ZooGame game) {
        long elapsedSeconds = Duration.between(game.getStartedAt(), LocalDateTime.now()).getSeconds();
        return Math.max(0, GAME_SECONDS - (int) elapsedSeconds);
    }

    private String randomNeed(String previousNeed) {
        List<String> candidates = NEEDS.stream()
                .filter(need -> !need.equals(previousNeed))
                .toList();
        return candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
    }

    private ZooCareResponse toResponse(ZooGame game, User user, Boolean correct, String message) {
        return toResponse(game, user, correct, message, getRemainingCareToday(user));
    }

    private ZooCareResponse toResponse(
            ZooGame game, User user, Boolean correct, String message, int remainingToday) {
        return ZooCareResponse.builder()
                .gameId(game.getId())
                .status(game.getStatus())
                .currentNeed(game.getCurrentNeed())
                .score(game.getScore())
                .remainingSeconds(getRemainingSeconds(game))
                .remainingToday(remainingToday)
                .pointsAwarded(game.getPointsAwarded())
                .pointsDeducted(game.getPointsDeducted())
                .totalPoints(user.getTotalPoints())
                .correct(correct)
                .message(message)
                .build();
    }

    private int getRemainingCareToday(User user) {
        int dailyLimit = user.getZooDailyCareLimit() != null
                ? user.getZooDailyCareLimit()
                : DEFAULT_ZOO_DAILY_CARE_LIMIT;
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        Long careCount = zooCareRecordMapper.selectCount(
                new LambdaQueryWrapper<ZooCareRecord>()
                        .eq(ZooCareRecord::getUserId, user.getId())
                        .ge(ZooCareRecord::getCaredAt, startOfDay)
                        .lt(ZooCareRecord::getCaredAt, startOfDay.plusDays(1)));
        return Math.max(0, dailyLimit - careCount.intValue());
    }
}
