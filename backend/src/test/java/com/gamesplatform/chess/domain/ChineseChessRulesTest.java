package com.gamesplatform.chess.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ChineseChessRulesTest {
    @Test
    void initialPositionAllowsPawnAndHorseButBlocksRook() {
        int[][] board = ChineseChessRules.initialBoard();
        assertTrue(ChineseChessRules.isLegalMove(board, 6, 0, 5, 0, true));
        assertTrue(ChineseChessRules.isLegalMove(board, 9, 1, 7, 2, true));
        assertFalse(ChineseChessRules.isLegalMove(board, 9, 0, 9, 2, true));
    }

    @Test
    void horseLegAndElephantRiverAreEnforced() {
        int[][] board = new int[10][9];
        board[9][4] = ChineseChessRules.GENERAL;
        board[0][3] = -ChineseChessRules.GENERAL;
        board[7][4] = ChineseChessRules.HORSE;
        board[6][4] = ChineseChessRules.PAWN;
        assertFalse(ChineseChessRules.isLegalMove(board, 7, 4, 5, 5, true));

        board[7][4] = ChineseChessRules.ELEPHANT;
        board[6][4] = 0;
        assertTrue(ChineseChessRules.isLegalMove(board, 7, 4, 5, 2, true));
        board[5][2] = ChineseChessRules.ELEPHANT;
        assertFalse(ChineseChessRules.isLegalMove(board, 5, 2, 3, 4, true));
    }

    @Test
    void cannonNeedsExactlyOneScreenToCapture() {
        int[][] board = new int[10][9];
        board[9][4] = ChineseChessRules.GENERAL;
        board[0][3] = -ChineseChessRules.GENERAL;
        board[7][1] = ChineseChessRules.CANNON;
        board[4][1] = ChineseChessRules.PAWN;
        board[1][1] = -ChineseChessRules.ROOK;
        assertTrue(ChineseChessRules.isLegalMove(board, 7, 1, 1, 1, true));
        board[4][1] = 0;
        assertFalse(ChineseChessRules.isLegalMove(board, 7, 1, 1, 1, true));
    }

    @Test
    void moveCannotExposeFlyingGenerals() {
        int[][] board = new int[10][9];
        board[9][4] = ChineseChessRules.GENERAL;
        board[0][4] = -ChineseChessRules.GENERAL;
        board[5][4] = ChineseChessRules.ROOK;
        assertFalse(ChineseChessRules.isLegalMove(board, 5, 4, 5, 3, true));
    }

    @Test
    void detectsWhenCurrentSideIsInCheck() {
        int[][] board = new int[10][9];
        board[9][4] = ChineseChessRules.GENERAL;
        board[0][3] = -ChineseChessRules.GENERAL;
        board[5][4] = -ChineseChessRules.ROOK;

        assertTrue(ChineseChessRules.isInCheck(board, true));
        assertFalse(ChineseChessRules.isInCheck(board, false));
    }
}
