package com.gamesplatform.sudoku.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubmitGameResponse {
    private boolean success;
    private int score;
    private int pointsEarned;
    private String message;
    private Integer newLevel;
    private Integer totalPoints;
}
