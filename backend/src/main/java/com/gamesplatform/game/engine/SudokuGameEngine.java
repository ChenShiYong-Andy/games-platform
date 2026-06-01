package com.gamesplatform.game.engine;

import com.gamesplatform.game.domain.GameResult;
import com.gamesplatform.game.domain.GameSession;
import com.gamesplatform.game.domain.GameSubmitCommand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * 数独游戏引擎。
 */
@Component
public class SudokuGameEngine implements GameEngine {

    /**
     * 数独生成器。
     */
    private final SudokuGenerator generator;

    /**
     * 创建数独游戏引擎。
     *
     * @param generator 数独生成器。
     */
    public SudokuGameEngine(SudokuGenerator generator) {
        this.generator = generator;
    }

    /**
     * 查询游戏类型。
     *
     * @return 处理结果。
     */
    @Override
    public String getGameType() {
        return "SUDOKU";
    }

    /**
     * 创建游戏。
     *
     * @param difficulty 游戏难度。
     * @return 处理结果。
     */
    @Override
    public GameSession createGame(String difficulty) {
        String normalized = difficulty.toUpperCase();
        int gridSize = generator.getGridSize(normalized);
        int[][] solution = generator.generateComplete(gridSize);
        int cellsToRemove = generator.getCellsToRemove(normalized);
        int[][] puzzle = generator.createPuzzle(solution, cellsToRemove);
        return GameSession.builder()
                .gameType(getGameType())
                .difficulty(normalized)
                .gridSize(gridSize)
                .puzzle(puzzle)
                .solution(solution)
                .build();
    }

    /**
     * 提交游戏结果。
     *
     * @param command 游戏提交命令。
     * @return 处理结果。
     */
    public GameResult submit(GameSubmitCommand command) {
        throw new UnsupportedOperationException("Use submitWithSolution instead");
    }

    /**
     * 根据答案提交游戏结果。
     *
     * @param command 游戏提交命令。
     * @param solution 答案棋盘。
     * @param difficulty 游戏难度。
     * @return 处理结果。
     */
    public GameResult submitWithSolution(GameSubmitCommand command, int[][] solution, String difficulty) {
        int[][] board = command.getBoard();
        if (!generator.isBoardComplete(board)) {
            return GameResult.builder()
                    .success(false)
                    .message("数独尚未完成")
                    .build();
        }
        if (!generator.isBoardCorrect(board, solution)) {
            return GameResult.builder()
                    .success(false)
                    .message("答案不正确，请检查")
                    .build();
        }
        int score = calculateScore(difficulty, command.getElapsedSeconds(),
                command.getHintsUsed(), command.getMistakes());
        return GameResult.builder()
                .success(true)
                .score(score)
                .pointsEarned(score)
                .message("恭喜通关！")
                .build();
    }

    /**
     * 校验落子。
     *
     * @param board 当前棋盘。
     * @param row 行索引。
     * @param col 列索引。
     * @param value 填入值。
     * @param solution 答案棋盘。
     * @return 处理结果。
     */
    @Override
    public boolean validateMove(int[][] board, int row, int col, int value, int[][] solution) {
        if (value == 0) return true;
        return value == solution[row][col];
    }

    /**
     * 获取提示。
     *
     * @param board 当前棋盘。
     * @param solution 答案棋盘。
     * @return 处理结果。
     */
    @Override
    public int[] getHint(int[][] board, int[][] solution) {
        int size = board.length;
        List<int[]> empty = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 0) {
                    empty.add(new int[]{r, c});
                }
            }
        }
        if (empty.isEmpty()) return null;
        int[] pos = empty.get(new Random().nextInt(empty.size()));
        return new int[]{pos[0], pos[1], solution[pos[0]][pos[1]]};
    }

    /**
     * 计算游戏得分。
     *
     * @param difficulty 游戏难度。
     * @param elapsedSeconds 耗时秒数。
     * @param hintsUsed 已使用提示次数。
     * @param mistakes 错误次数。
     * @return 处理结果。
     */
    public int calculateScore(String difficulty, int elapsedSeconds, int hintsUsed, int mistakes) {
        int base = switch (difficulty.toUpperCase()) {
            case "EASY" -> 20;
            case "MEDIUM" -> 30;
            case "HARD" -> 50;
            default -> 20;
        };
        int timeBonus = Math.max(0, 600 - elapsedSeconds) / 15;
        int hintPenalty = hintsUsed * 5;
        int mistakePenalty = mistakes * 3;
        return Math.max(10, base + timeBonus - hintPenalty - mistakePenalty);
    }
}
