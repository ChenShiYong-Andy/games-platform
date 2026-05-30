package com.gamesplatform.sudoku.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HintResponse {
    private int row;
    private int col;
    private int value;
}
