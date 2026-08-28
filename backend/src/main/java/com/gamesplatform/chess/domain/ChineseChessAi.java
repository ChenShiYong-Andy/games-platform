package com.gamesplatform.chess.domain;

/** 中国象棋人机对手，遍历合法走法并优先吃高价值棋子和将军。 */
public final class ChineseChessAi {
    private static final int[] PIECE_VALUES = {0, 500, 300, 250, 250, 100_000, 450, 100};

    private ChineseChessAi() {
    }

    /** 返回 {@code {起点行, 起点列, 终点行, 终点列}}，无合法走法时返回 {@code null}。 */
    public static int[] chooseMove(int[][] board, boolean red) {
        int bestScore = Integer.MIN_VALUE;
        int[] best = null;
        for (int fromRow = 0; fromRow < ChineseChessRules.ROWS; fromRow++) {
            for (int fromCol = 0; fromCol < ChineseChessRules.COLS; fromCol++) {
                int piece = board[fromRow][fromCol];
                if (piece == 0 || (piece > 0) != red) continue;
                for (int toRow = 0; toRow < ChineseChessRules.ROWS; toRow++) {
                    for (int toCol = 0; toCol < ChineseChessRules.COLS; toCol++) {
                        if (!ChineseChessRules.isLegalMove(board, fromRow, fromCol, toRow, toCol, red)) continue;
                        int captured = board[toRow][toCol];
                        int score = PIECE_VALUES[Math.abs(captured)] * 10
                                - PIECE_VALUES[Math.abs(piece)] / 20
                                + centerScore(toRow, toCol);
                        board[toRow][toCol] = piece;
                        board[fromRow][fromCol] = 0;
                        if (ChineseChessRules.isInCheck(board, !red)) score += 800;
                        board[fromRow][fromCol] = piece;
                        board[toRow][toCol] = captured;
                        if (score > bestScore) {
                            bestScore = score;
                            best = new int[]{fromRow, fromCol, toRow, toCol};
                        }
                    }
                }
            }
        }
        return best;
    }

    private static int centerScore(int row, int col) {
        return 12 - Math.abs(row * 2 - 9) - Math.abs(col - 4);
    }
}
