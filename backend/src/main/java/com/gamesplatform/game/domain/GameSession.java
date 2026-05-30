package com.gamesplatform.game.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GameSession {

    private Long gameId;
    private String gameType;
    private String difficulty;
    private Integer gridSize;
    private int[][] puzzle;
    private int[][] solution;
}
