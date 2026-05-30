package com.gamesplatform.ranking.service;

import com.gamesplatform.ranking.dto.RankingEntry;
import com.gamesplatform.user.entity.User;
import com.gamesplatform.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RankingService {

    private static final String TOTAL_POINTS_KEY = "ranking:total_points";
    private static final String WEEKLY_POINTS_PREFIX = "ranking:weekly:";
    private static final String SUDOKU_SPEED_PREFIX = "ranking:sudoku_speed:";

    private final StringRedisTemplate redisTemplate;
    private final UserMapper userMapper;

    public void updateTotalPoints(Long userId, int totalPoints) {
        redisTemplate.opsForZSet().add(TOTAL_POINTS_KEY, String.valueOf(userId), totalPoints);
    }

    public void updateWeeklyPoints(Long userId, int pointsAdded) {
        String key = getWeeklyKey();
        Double current = redisTemplate.opsForZSet().score(key, String.valueOf(userId));
        double newScore = (current != null ? current : 0) + pointsAdded;
        redisTemplate.opsForZSet().add(key, String.valueOf(userId), newScore);
    }

    public void updateSudokuSpeed(Long userId, String difficulty, int elapsedSeconds) {
        String key = SUDOKU_SPEED_PREFIX + difficulty.toUpperCase();
        Double current = redisTemplate.opsForZSet().score(key, String.valueOf(userId));
        if (current == null || elapsedSeconds < current.intValue()) {
            redisTemplate.opsForZSet().add(key, String.valueOf(userId), elapsedSeconds);
        }
    }

    public List<RankingEntry> getTotalPointsRanking(int limit) {
        return getRanking(TOTAL_POINTS_KEY, limit, false);
    }

    public List<RankingEntry> getWeeklyPointsRanking(int limit) {
        return getRanking(getWeeklyKey(), limit, false);
    }

    public List<RankingEntry> getSudokuSpeedRanking(String difficulty, int limit) {
        return getRanking(SUDOKU_SPEED_PREFIX + difficulty.toUpperCase(), limit, true);
    }

    private List<RankingEntry> getRanking(String key, int limit, boolean ascending) {
        Set<ZSetOperations.TypedTuple<String>> tuples;
        if (ascending) {
            tuples = redisTemplate.opsForZSet().rangeWithScores(key, 0, limit - 1);
        } else {
            tuples = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, limit - 1);
        }

        List<RankingEntry> entries = new ArrayList<>();
        if (tuples == null) return entries;

        int rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            Long userId = Long.parseLong(tuple.getValue());
            User user = userMapper.selectById(userId);
            if (user != null) {
                entries.add(RankingEntry.builder()
                        .userId(userId)
                        .nickname(user.getNickname())
                        .avatarUrl(user.getAvatarUrl())
                        .score(tuple.getScore().intValue())
                        .rank(rank++)
                        .build());
            }
        }
        return entries;
    }

    private String getWeeklyKey() {
        LocalDate now = LocalDate.now();
        int week = now.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
        int year = now.getYear();
        return WEEKLY_POINTS_PREFIX + year + ":w" + week;
    }
}
