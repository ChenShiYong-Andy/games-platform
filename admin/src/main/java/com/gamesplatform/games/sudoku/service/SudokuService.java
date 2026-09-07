package com.gamesplatform.games.sudoku.service;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.games.domain.GameResult;
import com.gamesplatform.games.domain.GameSession;
import com.gamesplatform.games.domain.GameSubmitCommand;
import com.gamesplatform.games.engine.SudokuGameEngine;
import com.gamesplatform.system.points.service.PointsService;
import com.gamesplatform.school.ranking.service.RankingService;
import com.gamesplatform.games.sudoku.dto.*;
import com.gamesplatform.games.sudoku.entity.SudokuGame;
import com.gamesplatform.games.sudoku.mapper.SudokuGameMapper;
import com.gamesplatform.system.user.entity.User;
import com.gamesplatform.system.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * 数独业务服务。
 */
@Service
@RequiredArgsConstructor
public class SudokuService {

    /**
     * 数独游戏引擎。
     */
    @Schema(description = "数独游戏引擎")
    private final SudokuGameEngine sudokuGameEngine;
    /**
     * 数独游戏记录数据访问组件。
     */
    @Schema(description = "数独游戏记录数据访问组件")
    private final SudokuGameMapper sudokuGameMapper;
    /**
     * JSON 序列化组件。
     */
    @Schema(description = "JSON 序列化组件")
    private final ObjectMapper objectMapper;
    /**
     * 积分服务。
     */
    @Schema(description = "积分服务")
    private final PointsService pointsService;
    /**
     * 排行榜服务。
     */
    @Schema(description = "排行榜服务")
    private final RankingService rankingService;
    /**
     * 用户数据访问组件。
     */
    @Schema(description = "用户数据访问组件")
    private final UserMapper userMapper;


    /**
     * 日期时间格式化器。
     */
    @Schema(description = "日期时间格式化器")
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    /**
     * 创建游戏。
     *
     * @param userId 用户 ID。
     * @param difficulty 游戏难度。
     * @return 处理结果。
     */
    @Transactional
    public GameResponse createGame(Long userId, String difficulty) {
        if (difficulty == null || difficulty.isBlank()) {
            difficulty = "EASY";
        }
        String normalizedDifficulty = difficulty.toUpperCase();
        validateDifficulty(normalizedDifficulty);

        GameSession session = sudokuGameEngine.createGame(normalizedDifficulty);

        SudokuGame game = new SudokuGame();
        game.setUserId(userId);
        game.setDifficulty(normalizedDifficulty);
        game.setPuzzleJson(toJson(session.getPuzzle()));
        game.setSolutionJson(toJson(session.getSolution()));
        game.setStatus("IN_PROGRESS");
        game.setElapsedSeconds(0);
        game.setHintsUsed(0);
        game.setMistakes(0);
        game.setScore(0);
        game.setStartedAt(LocalDateTime.now());
        sudokuGameMapper.insert(game);

        return toGameResponse(game, session.getPuzzle());
    }

    /**
     * 查询游戏。
     *
     * @param userId 用户 ID。
     * @param gameId 游戏 ID。
     * @return 处理结果。
     */
    public GameResponse getGame(Long userId, Long gameId) {
        SudokuGame game = getGameEntity(userId, gameId);
        return toGameResponse(game, fromJson(game.getPuzzleJson()));
    }

    /**
     * 查询游戏历史。
     *
     * @param userId 用户 ID。
     * @param limit 查询数量上限。
     * @return 处理结果。
     */
    public List<GameResponse> getGameHistory(Long userId, int limit) {
        List<SudokuGame> games = sudokuGameMapper.selectList(
                new LambdaQueryWrapper<SudokuGame>()
                        .eq(SudokuGame::getUserId, userId)
                        .orderByDesc(SudokuGame::getStartedAt)
                        .last("LIMIT " + limit));
        return games.stream().map(g -> toGameResponse(g, fromJson(g.getPuzzleJson()))).toList();
    }

    /**
     * 校验落子。
     *
     * @param userId 用户 ID。
     * @param gameId 游戏 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    public ValidateMoveResponse validateMove(Long userId, Long gameId, ValidateMoveRequest request) {
        SudokuGame game = getGameEntity(userId, gameId);
        if (!"IN_PROGRESS".equals(game.getStatus())) {
            throw new BusinessException("游戏已结束");
        }
        int[][] solution = fromJson(game.getSolutionJson());
        int[][] board = fromJson(game.getPuzzleJson());
        boolean valid = sudokuGameEngine.validateMove(board, request.getRow(), request.getCol(),
                request.getValue(), solution);
        return ValidateMoveResponse.builder().valid(valid).build();
    }

    /**
     * 提交游戏。
     *
     * @param userId 用户 ID。
     * @param gameId 游戏 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Transactional
    public SubmitGameResponse submitGame(Long userId, Long gameId, SubmitGameRequest request) {
        SudokuGame game = getGameEntity(userId, gameId);
        if (!"IN_PROGRESS".equals(game.getStatus())) {
            throw new BusinessException("游戏已结束");
        }
        int[][] solution = fromJson(game.getSolutionJson());
        GameSubmitCommand command = new GameSubmitCommand();
        command.setGameId(gameId);
        command.setUserId(userId);
        command.setBoard(request.getBoard());
        command.setElapsedSeconds(request.getElapsedSeconds());
        command.setHintsUsed(request.getHintsUsed());
        command.setMistakes(request.getMistakes());

        GameResult result = sudokuGameEngine.submitWithSolution(command, solution, game.getDifficulty());
        if (!result.isSuccess()) {
            return SubmitGameResponse.builder()
                    .success(false)
                    .message(result.getMessage())
                    .build();
        }

        game.setStatus("COMPLETED");
        game.setElapsedSeconds(request.getElapsedSeconds());
        game.setHintsUsed(request.getHintsUsed());
        game.setMistakes(request.getMistakes());
        game.setScore(result.getScore());
        game.setCompletedAt(LocalDateTime.now());
        sudokuGameMapper.updateById(game);

        int totalPoints = pointsService.awardPoints(userId, result.getPointsEarned(),
                "SUDOKU_COMPLETE", gameId, "完成" + game.getDifficulty() + "难度数独");

        rankingService.updateSudokuSpeed(userId, game.getDifficulty(), request.getElapsedSeconds());

        User user = userMapper.selectById(userId);
        int totalClears = user.getTotalClears() + 1;
        user.setTotalClears(totalClears);
        userMapper.updateById(user);

        return SubmitGameResponse.builder()
                .success(true)
                .score(result.getScore())
                .pointsEarned(result.getPointsEarned())
                .message(result.getMessage())
                .newLevel(user.getLevel())
                .totalPoints(totalPoints)
                .build();
    }

    /**
     * 查询并校验用户的数独游戏记录。
     *
     * @param userId 用户标识
     * @param gameId 游戏标识
     * @return 操作结果
     */
    private SudokuGame getGameEntity(Long userId, Long gameId) {
        SudokuGame game = sudokuGameMapper.selectById(gameId);
        if (game == null || !game.getUserId().equals(userId)) {
            throw new BusinessException("游戏不存在");
        }
        return game;
    }

    /**
     * 校验数独难度参数。
     *
     * @param difficulty 游戏难度
     */
    private void validateDifficulty(String difficulty) {
        Set<String> valid = Set.of("EASY", "MEDIUM", "HARD");
        if (!valid.contains(difficulty)) {
            throw new BusinessException("无效的难度: " + difficulty);
        }
    }

    /**
     * 将数独游戏实体转换为接口响应。
     *
     * @param game 游戏记录
     * @param puzzle puzzle 参数
     * @return 操作结果
     */
    private GameResponse toGameResponse(SudokuGame game, int[][] puzzle) {
        return GameResponse.builder()
                .id(game.getId())
                .difficulty(game.getDifficulty())
                .gridSize(puzzle.length)
                .puzzle(puzzle)
                .status(game.getStatus())
                .elapsedSeconds(game.getElapsedSeconds())
                .hintsUsed(game.getHintsUsed())
                .mistakes(game.getMistakes())
                .score(game.getScore())
                .startedAt(game.getStartedAt() != null ? game.getStartedAt().format(FORMATTER) : null)
                .completedAt(game.getCompletedAt() != null ? game.getCompletedAt().format(FORMATTER) : null)
                .build();
    }

    /**
     * 将棋盘序列化为 JSON。
     *
     * @param board 棋盘数据
     * @return 对应的文本结果
     */
    private String toJson(int[][] board) {
        try {
            return objectMapper.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            throw new BusinessException("数据序列化失败");
        }
    }

    /**
     * 将 JSON 反序列化为棋盘。
     *
     * @param json 棋盘 JSON 数据
     * @return 二维棋盘数据
     */
    private int[][] fromJson(String json) {
        try {
            return objectMapper.readValue(json, int[][].class);
        } catch (JsonProcessingException e) {
            throw new BusinessException("数据反序列化失败");
        }
    }
}
