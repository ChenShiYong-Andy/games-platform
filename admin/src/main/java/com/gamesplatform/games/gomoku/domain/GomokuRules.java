package com.gamesplatform.games.gomoku.domain;

import io.swagger.v3.oas.annotations.media.Schema;

/** 五子棋棋盘规则。 */
public final class GomokuRules {
    /**
     * 五子棋棋盘边长。
     */
    @Schema(description = "五子棋棋盘边长")
    public static final int BOARD_SIZE = 15;


    /**
     * 创建GomokuRules实例。
     */
    private GomokuRules() {
    }

    /**
     * 判断指定落子是否形成五子连线。
     *
     * @param board 棋盘数据
     * @param row 行坐标
     * @param col 列坐标
     * @param stone 棋子颜色编号
     * @return 操作结果
     */
    public static boolean isWinningMove(int[][] board, int row, int col, int stone) {
        int[][] directions = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};
        for (int[] direction : directions) {
            int count = 1
                    + count(board, row, col, direction[0], direction[1], stone)
                    + count(board, row, col, -direction[0], -direction[1], stone);
            if (count >= 5) {
                return true;
            }
        }
        return false;
    }

    /**
     * 统计指定方向上的连续同色棋子数量。
     *
     * @param board 棋盘数据
     * @param row 行坐标
     * @param col 列坐标
     * @param dr 行方向增量
     * @param dc 列方向增量
     * @param stone 棋子颜色编号
     * @return 操作结果
     */
    private static int count(int[][] board, int row, int col, int dr, int dc, int stone) {
        int count = 0;
        for (int r = row + dr, c = col + dc;
             r >= 0 && r < BOARD_SIZE && c >= 0 && c < BOARD_SIZE && board[r][c] == stone;
             r += dr, c += dc) {
            count++;
        }
        return count;
    }
}
