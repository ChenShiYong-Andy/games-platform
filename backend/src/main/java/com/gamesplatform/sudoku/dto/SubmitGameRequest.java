package com.gamesplatform.sudoku.dto;

import lombok.Data;

@Data
public class SubmitGameRequest {

    private int[][] board;
    private int elapsedSeconds;
    private int hintsUsed;
    private int mistakes;
}
