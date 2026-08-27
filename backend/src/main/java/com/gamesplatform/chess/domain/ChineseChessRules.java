package com.gamesplatform.chess.domain;

/** 中国象棋棋盘、棋子移动和将军规则。正数为红方，负数为黑方。 */
public final class ChineseChessRules {
    public static final int ROWS = 10;
    public static final int COLS = 9;
    public static final int ROOK = 1;
    public static final int HORSE = 2;
    public static final int ELEPHANT = 3;
    public static final int ADVISOR = 4;
    public static final int GENERAL = 5;
    public static final int CANNON = 6;
    public static final int PAWN = 7;

    private ChineseChessRules() {
    }

    /** 创建标准开局棋盘。 */
    public static int[][] initialBoard() {
        int[][] board = new int[ROWS][COLS];
        board[0] = new int[]{-ROOK, -HORSE, -ELEPHANT, -ADVISOR, -GENERAL, -ADVISOR, -ELEPHANT, -HORSE, -ROOK};
        board[2][1] = board[2][7] = -CANNON;
        for (int col = 0; col < COLS; col += 2) board[3][col] = -PAWN;
        board[9] = new int[]{ROOK, HORSE, ELEPHANT, ADVISOR, GENERAL, ADVISOR, ELEPHANT, HORSE, ROOK};
        board[7][1] = board[7][7] = CANNON;
        for (int col = 0; col < COLS; col += 2) board[6][col] = PAWN;
        return board;
    }

    /** 校验并执行走子；走子方不能让自己的将帅处于被将军状态。 */
    public static boolean isLegalMove(int[][] board, int fr, int fc, int tr, int tc, boolean red) {
        if (!inside(fr, fc) || !inside(tr, tc) || (fr == tr && fc == tc)) return false;
        int piece = board[fr][fc];
        if (piece == 0 || (piece > 0) != red || (board[tr][tc] != 0 && (board[tr][tc] > 0) == red)) return false;
        if (!isPseudoLegal(board, fr, fc, tr, tc, piece)) return false;

        int captured = board[tr][tc];
        board[tr][tc] = piece;
        board[fr][fc] = 0;
        boolean safe = findGeneral(board, red) != null && !isInCheck(board, red);
        board[fr][fc] = piece;
        board[tr][tc] = captured;
        return safe;
    }

    /** 判断一方是否已无任何合法走法。 */
    public static boolean hasLegalMove(int[][] board, boolean red) {
        for (int fr = 0; fr < ROWS; fr++) {
            for (int fc = 0; fc < COLS; fc++) {
                if (board[fr][fc] != 0 && (board[fr][fc] > 0) == red) {
                    for (int tr = 0; tr < ROWS; tr++) {
                        for (int tc = 0; tc < COLS; tc++) {
                            if (isLegalMove(board, fr, fc, tr, tc, red)) return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /** 判断一方将帅是否正受到攻击。 */
    public static boolean isInCheck(int[][] board, boolean red) {
        int[] general = findGeneral(board, red);
        if (general == null) return true;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int attacker = board[row][col];
                if (attacker != 0 && (attacker > 0) != red
                        && isPseudoLegal(board, row, col, general[0], general[1], attacker)) return true;
            }
        }
        return false;
    }

    private static boolean isPseudoLegal(int[][] board, int fr, int fc, int tr, int tc, int piece) {
        int dr = tr - fr;
        int dc = tc - fc;
        boolean red = piece > 0;
        return switch (Math.abs(piece)) {
            case ROOK -> (dr == 0 || dc == 0) && countBetween(board, fr, fc, tr, tc) == 0;
            case CANNON -> (dr == 0 || dc == 0)
                    && countBetween(board, fr, fc, tr, tc) == (board[tr][tc] == 0 ? 0 : 1);
            case HORSE -> isHorseMove(board, fr, fc, tr, tc, dr, dc);
            case ELEPHANT -> Math.abs(dr) == 2 && Math.abs(dc) == 2
                    && board[fr + dr / 2][fc + dc / 2] == 0 && (red ? tr >= 5 : tr <= 4);
            case ADVISOR -> Math.abs(dr) == 1 && Math.abs(dc) == 1 && inPalace(tr, tc, red);
            case GENERAL -> isGeneralMove(board, fr, fc, tr, tc, dr, dc, red);
            case PAWN -> isPawnMove(fr, dr, dc, red);
            default -> false;
        };
    }

    private static boolean isHorseMove(int[][] board, int fr, int fc, int tr, int tc, int dr, int dc) {
        if (Math.abs(dr) == 2 && Math.abs(dc) == 1) return board[fr + dr / 2][fc] == 0;
        if (Math.abs(dr) == 1 && Math.abs(dc) == 2) return board[fr][fc + dc / 2] == 0;
        return false;
    }

    private static boolean isGeneralMove(int[][] board, int fr, int fc, int tr, int tc, int dr, int dc, boolean red) {
        if (Math.abs(board[tr][tc]) == GENERAL && fc == tc) return countBetween(board, fr, fc, tr, tc) == 0;
        return Math.abs(dr) + Math.abs(dc) == 1 && inPalace(tr, tc, red);
    }

    private static boolean isPawnMove(int row, int dr, int dc, boolean red) {
        int forward = red ? -1 : 1;
        if (dr == forward && dc == 0) return true;
        boolean crossedRiver = red ? row <= 4 : row >= 5;
        return crossedRiver && dr == 0 && Math.abs(dc) == 1;
    }

    private static int countBetween(int[][] board, int fr, int fc, int tr, int tc) {
        if (fr != tr && fc != tc) return -1;
        int dr = Integer.compare(tr, fr);
        int dc = Integer.compare(tc, fc);
        int count = 0;
        for (int r = fr + dr, c = fc + dc; r != tr || c != tc; r += dr, c += dc) {
            if (board[r][c] != 0) count++;
        }
        return count;
    }

    private static boolean inPalace(int row, int col, boolean red) {
        return col >= 3 && col <= 5 && (red ? row >= 7 && row <= 9 : row >= 0 && row <= 2);
    }

    private static int[] findGeneral(int[][] board, boolean red) {
        int target = red ? GENERAL : -GENERAL;
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) if (board[row][col] == target) return new int[]{row, col};
        }
        return null;
    }

    private static boolean inside(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }
}
