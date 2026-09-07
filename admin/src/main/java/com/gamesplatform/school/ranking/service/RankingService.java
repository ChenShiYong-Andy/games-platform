package com.gamesplatform.school.ranking.service;

import com.gamesplatform.school.ranking.dto.RankingEntry;

import java.util.List;

/**
 * 排行榜业务服务。
 */
public interface RankingService {

    /**
     * 更新用户总积分排行。
     *
     * @param userId 用户 ID。
     * @param totalPoints 最新总积分。
     */
    void updateTotalPoints(Long userId, int totalPoints);

    /**
     * 累加用户周积分。
     *
     * @param userId 用户 ID。
     * @param pointsAdded 本次积分变动值。
     */
    void updateWeeklyPoints(Long userId, int pointsAdded);

    /**
     * 更新用户数独速通成绩。
     *
     * @param userId 用户 ID。
     * @param difficulty 难度标识。
     * @param elapsedSeconds 用时秒数。
     */
    void updateSudokuSpeed(Long userId, String difficulty, int elapsedSeconds);

    /**
     * 查询总积分排行榜。
     *
     * @param limit 返回数量上限。
     * @return 排行榜列表。
     */
    List<RankingEntry> getTotalPointsRanking(int limit);

    /**
     * 查询本周积分排行榜。
     *
     * @param limit 返回数量上限。
     * @return 排行榜列表。
     */
    List<RankingEntry> getWeeklyPointsRanking(int limit);

    /**
     * 查询指定难度的数独速通排行榜。
     *
     * @param difficulty 难度标识。
     * @param limit 返回数量上限。
     * @return 排行榜列表。
     */
    List<RankingEntry> getSudokuSpeedRanking(String difficulty, int limit);
}
