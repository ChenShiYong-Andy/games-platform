package com.gamesplatform.sudoku.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameResponse {

    private Long id;
    private String difficulty;
    private Integer gridSize;
    private int[][] puzzle;
    private String status;
    private Integer elapsedSeconds;
    private Integer hintsUsed;
    private Integer mistakes;
    private Integer score;
    private String startedAt;
    private String completedAt;
}
