package com.gamesplatform.games.gomoku.domain;

/** 五子棋人机对手，优先取胜、拦截对手，再按棋形与中心位置选点。 */
public final class GomokuAi {
    /**
     * 五子棋连线检测方向。
     */
    private static final int[][] DIRECTIONS = {{1, 0}, {0, 1}, {1, 1}, {1, -1}};


    /**
     * 创建GomokuAi实例。
     */
    private GomokuAi() {
    }

    /**
     * 根据当前棋盘选择电脑走法。
     *
     * @param board 棋盘数据
     * @param aiStone 电脑棋子颜色编号
     * @return 操作结果
     */
    public static int[] chooseMove(int[][] board, int aiStone) {
        int opponentStone = aiStone == 1 ? 2 : 1;
        int[] winning = findImmediateMove(board, aiStone);
        if (winning != null) return winning;
        int[] blocking = findImmediateMove(board, opponentStone);
        if (blocking != null) return blocking;

        int center = GomokuRules.BOARD_SIZE / 2;
        int bestScore = Integer.MIN_VALUE;
        int[] best = null;
        for (int row = 0; row < GomokuRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuRules.BOARD_SIZE; col++) {
                if (board[row][col] != 0) continue;
                int score = potential(board, row, col, aiStone) * 3
                        + potential(board, row, col, opponentStone) * 2
                        - Math.abs(row - center) - Math.abs(col - center);
                if (score > bestScore) {
                    bestScore = score;
                    best = new int[]{row, col};
                }
            }
        }
        return best;
    }

    /**
     * 查找能够立即取胜或阻止对手取胜的位置。
     *
     * @param board 棋盘数据
     * @param stone 棋子颜色编号
     * @return 操作结果
     */
    private static int[] findImmediateMove(int[][] board, int stone) {
        for (int row = 0; row < GomokuRules.BOARD_SIZE; row++) {
            for (int col = 0; col < GomokuRules.BOARD_SIZE; col++) {
                if (board[row][col] != 0) continue;
                board[row][col] = stone;
                boolean wins = GomokuRules.isWinningMove(board, row, col, stone);
                board[row][col] = 0;
                if (wins) return new int[]{row, col};
            }
        }
        return null;
    }

    /**
     * 评估指定位置的连线潜力。
     *
     * @param board 棋盘数据
     * @param row 行坐标
     * @param col 列坐标
     * @param stone 棋子颜色编号
     * @return 操作结果
     */
    private static int potential(int[][] board, int row, int col, int stone) {
        int score = 0;
        for (int[] direction : DIRECTIONS) {
            int line = 1
                    + count(board, row, col, direction[0], direction[1], stone)
                    + count(board, row, col, -direction[0], -direction[1], stone);
            score += line * line;
        }
        return score;
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
        int result = 0;
        for (int r = row + dr, c = col + dc;
             r >= 0 && r < GomokuRules.BOARD_SIZE && c >= 0 && c < GomokuRules.BOARD_SIZE
                     && board[r][c] == stone;
             r += dr, c += dc) {
            result++;
        }
        return result;
    }
}
