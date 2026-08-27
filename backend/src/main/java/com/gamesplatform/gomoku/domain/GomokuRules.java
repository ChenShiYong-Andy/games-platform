package com.gamesplatform.gomoku.domain;

/** 五子棋棋盘规则。 */
public final class GomokuRules {
    public static final int BOARD_SIZE = 15;

    private GomokuRules() {
    }

    /** 判断指定落子是否形成至少五颗连续棋子。 */
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
