package com.gamesplatform.sudoku.dto;

import lombok.Data;

@Data
public class ValidateMoveRequest {
    private int row;
    private int col;
    private int value;
}
