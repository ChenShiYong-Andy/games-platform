package com.gamesplatform.gomoku.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GomokuRulesTest {
    @Test
    void recognizesWinsInAllDirections() {
        int[][] horizontal = boardWithLine(7, 3, 0, 1);
        int[][] vertical = boardWithLine(3, 7, 1, 0);
        int[][] diagonal = boardWithLine(3, 3, 1, 1);
        int[][] antiDiagonal = boardWithLine(3, 11, 1, -1);

        assertTrue(GomokuRules.isWinningMove(horizontal, 7, 5, 1));
        assertTrue(GomokuRules.isWinningMove(vertical, 5, 7, 1));
        assertTrue(GomokuRules.isWinningMove(diagonal, 5, 5, 1));
        assertTrue(GomokuRules.isWinningMove(antiDiagonal, 5, 9, 1));
    }

    @Test
    void doesNotTreatFourStonesAsWin() {
        int[][] board = new int[GomokuRules.BOARD_SIZE][GomokuRules.BOARD_SIZE];
        for (int col = 2; col < 6; col++) board[8][col] = 2;
        assertFalse(GomokuRules.isWinningMove(board, 8, 4, 2));
    }

    private int[][] boardWithLine(int row, int col, int dr, int dc) {
        int[][] board = new int[GomokuRules.BOARD_SIZE][GomokuRules.BOARD_SIZE];
        for (int i = 0; i < 5; i++) board[row + i * dr][col + i * dc] = 1;
        return board;
    }
}
