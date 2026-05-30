package com.gamesplatform.game.domain;

import lombok.Data;

@Data
public class GameSubmitCommand {

    private Long gameId;
    private Long userId;
    private int[][] board;
    private int elapsedSeconds;
    private int hintsUsed;
    private int mistakes;
}
