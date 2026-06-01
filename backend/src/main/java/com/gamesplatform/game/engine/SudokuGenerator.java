package com.gamesplatform.game.engine;

import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 数独棋盘生成器。
 */
@Component
public class SudokuGenerator {

    /**
     * 随机数生成器。
     */
    private static final Random RANDOM = new Random();

    /**
     * 查询难度对应的棋盘尺寸。
     *
     * @param difficulty 游戏难度。
     * @return 处理结果。
     */
    public int getGridSize(String difficulty) {
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> 4;
            case "MEDIUM" -> 6;
            case "HARD" -> 9;
            default -> 4;
        };
    }

    /**
     * 查询宫格行尺寸。
     *
     * @param gridSize 棋盘尺寸。
     * @return 处理结果。
     */
    public int getBoxRowSize(int gridSize) {
        return switch (gridSize) {
            case 4 -> 2;
            case 6 -> 2;
            case 9 -> 3;
            default -> 2;
        };
    }

    /**
     * 查询宫格列尺寸。
     *
     * @param gridSize 棋盘尺寸。
     * @return 处理结果。
     */
    public int getBoxColSize(int gridSize) {
        return switch (gridSize) {
            case 4 -> 2;
            case 6 -> 3;
            case 9 -> 3;
            default -> 2;
        };
    }

    /**
     * 生成完整棋盘。
     *
     * @param gridSize 棋盘尺寸。
     * @return 处理结果。
     */
    public int[][] generateComplete(int gridSize) {
        int[][] board = new int[gridSize][gridSize];
        fillBoard(board, gridSize);
        return board;
    }

    /**
     * 根据答案生成题目棋盘。
     *
     * @param solution 答案棋盘。
     * @param cellsToRemove 需要挖空的单元格数量。
     * @return 处理结果。
     */
    public int[][] createPuzzle(int[][] solution, int cellsToRemove) {
        int size = solution.length;
        int[][] puzzle = copyBoard(solution);
        List<int[]> positions = new ArrayList<>();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                positions.add(new int[]{r, c});
            }
        }
        Collections.shuffle(positions, RANDOM);
        int removed = 0;
        for (int[] pos : positions) {
            if (removed >= cellsToRemove) break;
            puzzle[pos[0]][pos[1]] = 0;
            removed++;
        }
        return puzzle;
    }

    /**
     * 查询需要挖空的单元格数量。
     *
     * @param difficulty 游戏难度。
     * @return 处理结果。
     */
    public int getCellsToRemove(String difficulty) {
        return switch (difficulty.toUpperCase()) {
            case "EASY" -> 5;    // 4×4，数字 1～4
            case "MEDIUM" -> 10; // 6×6，数字 1～6
            case "HARD" -> 18;   // 9×9，沿用原中等
            default -> 5;
        };
    }

    private boolean fillBoard(int[][] board, int gridSize) {
        int maxNum = gridSize;
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (board[row][col] == 0) {
                    List<Integer> nums = new ArrayList<>();
                    for (int i = 1; i <= maxNum; i++) nums.add(i);
                    Collections.shuffle(nums, RANDOM);
                    for (int num : nums) {
                        if (isValid(board, row, col, num, gridSize)) {
                            board[row][col] = num;
                            if (fillBoard(board, gridSize)) return true;
                            board[row][col] = 0;
                        }
                    }
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * 校验数字能否填入指定位置。
     *
     * @param board 当前棋盘。
     * @param row 行索引。
     * @param col 列索引。
     * @param num 待校验数字。
     * @param gridSize 棋盘尺寸。
     * @return 处理结果。
     */
    public boolean isValid(int[][] board, int row, int col, int num, int gridSize) {
        for (int c = 0; c < gridSize; c++) {
            if (board[row][c] == num) return false;
        }
        for (int r = 0; r < gridSize; r++) {
            if (board[r][col] == num) return false;
        }
        int boxRowSize = getBoxRowSize(gridSize);
        int boxColSize = getBoxColSize(gridSize);
        int boxRow = (row / boxRowSize) * boxRowSize;
        int boxCol = (col / boxColSize) * boxColSize;
        for (int r = boxRow; r < boxRow + boxRowSize; r++) {
            for (int c = boxCol; c < boxCol + boxColSize; c++) {
                if (board[r][c] == num) return false;
            }
        }
        return true;
    }

    /**
     * 检查棋盘是否填写完整。
     *
     * @param board 当前棋盘。
     * @return 处理结果。
     */
    public boolean isBoardComplete(int[][] board) {
        int size = board.length;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] == 0) return false;
            }
        }
        return true;
    }

    /**
     * 检查棋盘答案是否正确。
     *
     * @param board 当前棋盘。
     * @param solution 答案棋盘。
     * @return 处理结果。
     */
    public boolean isBoardCorrect(int[][] board, int[][] solution) {
        int size = board.length;
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (board[r][c] != solution[r][c]) return false;
            }
        }
        return true;
    }

    /**
     * 复制棋盘。
     *
     * @param board 当前棋盘。
     * @return 处理结果。
     */
    public int[][] copyBoard(int[][] board) {
        int size = board.length;
        int[][] copy = new int[size][size];
        for (int r = 0; r < size; r++) {
            System.arraycopy(board[r], 0, copy[r], 0, size);
        }
        return copy;
    }
}
