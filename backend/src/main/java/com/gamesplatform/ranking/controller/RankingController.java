package com.gamesplatform.ranking.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.ranking.dto.RankingEntry;
import com.gamesplatform.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 排行榜接口。
 */
@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    /**
     * 排行榜服务。
     */
    private final RankingService rankingService;

    /**
     * 查询总积分排行榜。
     *
     * @param limit 查询数量上限。
     * @return 处理结果。
     */
    @GetMapping("/total")
    public ApiResponse<List<RankingEntry>> getTotalRanking(
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(rankingService.getTotalPointsRanking(limit));
    }

    /**
     * 查询周积分排行榜。
     *
     * @param limit 查询数量上限。
     * @return 处理结果。
     */
    @GetMapping("/weekly")
    public ApiResponse<List<RankingEntry>> getWeeklyRanking(
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(rankingService.getWeeklyPointsRanking(limit));
    }

    /**
     * 查询数独速度排行榜。
     *
     * @param difficulty 游戏难度。
     * @param limit 查询数量上限。
     * @return 处理结果。
     */
    @GetMapping("/sudoku-speed")
    public ApiResponse<List<RankingEntry>> getSudokuSpeedRanking(
            @RequestParam(defaultValue = "EASY") String difficulty,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(rankingService.getSudokuSpeedRanking(difficulty, limit));
    }

}
