package com.gamesplatform.games.dashboard.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamesplatform.games.chess.entity.ChineseChessGame;
import com.gamesplatform.games.chess.mapper.ChineseChessGameMapper;
import com.gamesplatform.games.dashboard.dto.TodayGameStatsResponse;
import com.gamesplatform.games.gomoku.entity.GomokuGame;
import com.gamesplatform.games.gomoku.mapper.GomokuGameMapper;
import com.gamesplatform.games.sudoku.entity.SudokuGame;
import com.gamesplatform.games.sudoku.mapper.SudokuGameMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 游戏大厅数据服务。
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    /**
     * 数独游戏数据访问组件
     */
    @Schema(description = "数独游戏数据访问组件")
    private final SudokuGameMapper sudokuGameMapper;

    /**
     * 五子棋游戏数据访问组件
     */
    @Schema(description = "五子棋游戏数据访问组件")
    private final GomokuGameMapper gomokuGameMapper;

    /**
     * 中国象棋游戏数据访问组件
     */
    @Schema(description = "中国象棋游戏数据访问组件")
    private final ChineseChessGameMapper chineseChessGameMapper;

    /**
     * 统计当前用户自然日内参与的各类游戏次数。
     *
     * @param userId 用户标识
     * @return 今日游戏次数统计
     */
    public TodayGameStatsResponse getTodayGameStats(Long userId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        Long sudoku = sudokuGameMapper.selectCount(
                new LambdaQueryWrapper<SudokuGame>()
                        .eq(SudokuGame::getUserId, userId)
                        .ge(SudokuGame::getStartedAt, start)
                        .lt(SudokuGame::getStartedAt, end));
        Long gomoku = gomokuGameMapper.selectCount(
                new LambdaQueryWrapper<GomokuGame>()
                        .and(query -> query.eq(GomokuGame::getBlackPlayerId, userId)
                                .or()
                                .eq(GomokuGame::getWhitePlayerId, userId))
                        .ge(GomokuGame::getStartedAt, start)
                        .lt(GomokuGame::getStartedAt, end));
        Long chess = chineseChessGameMapper.selectCount(
                new LambdaQueryWrapper<ChineseChessGame>()
                        .and(query -> query.eq(ChineseChessGame::getRedPlayerId, userId)
                                .or()
                                .eq(ChineseChessGame::getBlackPlayerId, userId))
                        .ge(ChineseChessGame::getStartedAt, start)
                        .lt(ChineseChessGame::getStartedAt, end));

        return TodayGameStatsResponse.builder()
                .sudoku(sudoku)
                .gomoku(gomoku)
                .chess(chess)
                .build();
    }
}
