package com.gamesplatform.sudoku.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesplatform.achievement.service.AchievementService;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.game.domain.GameResult;
import com.gamesplatform.game.domain.GameSession;
import com.gamesplatform.game.domain.GameSubmitCommand;
import com.gamesplatform.game.engine.SudokuGameEngine;
import com.gamesplatform.points.service.PointsService;
import com.gamesplatform.ranking.service.RankingService;
import com.gamesplatform.sudoku.dto.*;
import com.gamesplatform.sudoku.entity.SudokuGame;
import com.gamesplatform.sudoku.mapper.SudokuGameMapper;
import com.gamesplatform.user.entity.User;
import com.gamesplatform.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SudokuService {

    private final SudokuGameEngine sudokuGameEngine;
    private final SudokuGameMapper sudokuGameMapper;
    private final ObjectMapper objectMapper;
    private final PointsService pointsService;
    private final RankingService rankingService;
    private final AchievementService achievementService;
    private final UserMapper userMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    public GameResponse getGame(Long userId, Long gameId) {
        SudokuGame game = getGameEntity(userId, gameId);
        return toGameResponse(game, fromJson(game.getPuzzleJson()));
    }

    public List<GameResponse> getGameHistory(Long userId, int limit) {
        List<SudokuGame> games = sudokuGameMapper.selectList(
                new LambdaQueryWrapper<SudokuGame>()
                        .eq(SudokuGame::getUserId, userId)
                        .orderByDesc(SudokuGame::getStartedAt)
                        .last("LIMIT " + limit));
        return games.stream().map(g -> toGameResponse(g, fromJson(g.getPuzzleJson()))).toList();
    }

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

    @Transactional
    public HintResponse getHint(Long userId, Long gameId) {
        SudokuGame game = getGameEntity(userId, gameId);
        if (!"IN_PROGRESS".equals(game.getStatus())) {
            throw new BusinessException("游戏已结束");
        }
        int[][] solution = fromJson(game.getSolutionJson());
        int[][] board = fromJson(game.getPuzzleJson());
        int[] hint = sudokuGameEngine.getHint(board, solution);
        if (hint == null) {
            throw new BusinessException("没有可用的提示");
        }
        game.setHintsUsed(game.getHintsUsed() + 1);
        sudokuGameMapper.updateById(game);
        return HintResponse.builder()
                .row(hint[0])
                .col(hint[1])
                .value(hint[2])
                .build();
    }

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

        achievementService.checkGameComplete(userId, game.getDifficulty(), totalClears);

        return SubmitGameResponse.builder()
                .success(true)
                .score(result.getScore())
                .pointsEarned(result.getPointsEarned())
                .message(result.getMessage())
                .newLevel(user.getLevel())
                .totalPoints(totalPoints)
                .build();
    }

    private SudokuGame getGameEntity(Long userId, Long gameId) {
        SudokuGame game = sudokuGameMapper.selectById(gameId);
        if (game == null || !game.getUserId().equals(userId)) {
            throw new BusinessException("游戏不存在");
        }
        return game;
    }

    private void validateDifficulty(String difficulty) {
        Set<String> valid = Set.of("EASY", "MEDIUM", "HARD");
        if (!valid.contains(difficulty)) {
            throw new BusinessException("无效的难度: " + difficulty);
        }
    }

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

    private String toJson(int[][] board) {
        try {
            return objectMapper.writeValueAsString(board);
        } catch (JsonProcessingException e) {
            throw new BusinessException("数据序列化失败");
        }
    }

    private int[][] fromJson(String json) {
        try {
            return objectMapper.readValue(json, int[][].class);
        } catch (JsonProcessingException e) {
            throw new BusinessException("数据反序列化失败");
        }
    }
}
