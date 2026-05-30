package com.gamesplatform.ranking.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.ranking.dto.RankingEntry;
import com.gamesplatform.ranking.service.RankingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/total")
    public ApiResponse<List<RankingEntry>> getTotalRanking(
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(rankingService.getTotalPointsRanking(limit));
    }

    @GetMapping("/weekly")
    public ApiResponse<List<RankingEntry>> getWeeklyRanking(
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(rankingService.getWeeklyPointsRanking(limit));
    }

    @GetMapping("/sudoku-speed")
    public ApiResponse<List<RankingEntry>> getSudokuSpeedRanking(
            @RequestParam(defaultValue = "EASY") String difficulty,
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(rankingService.getSudokuSpeedRanking(difficulty, limit));
    }
}
