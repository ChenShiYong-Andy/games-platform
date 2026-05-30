package com.gamesplatform.game.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameResult {

    private boolean success;
    private int score;
    private int pointsEarned;
    private String message;
}
