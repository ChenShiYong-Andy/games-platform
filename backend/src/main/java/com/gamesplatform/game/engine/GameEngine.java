package com.gamesplatform.game.engine;

import com.gamesplatform.game.domain.GameResult;
import com.gamesplatform.game.domain.GameSession;
import com.gamesplatform.game.domain.GameSubmitCommand;

public interface GameEngine {

    String getGameType();

    GameSession createGame(String difficulty);

    GameResult submit(GameSubmitCommand command);

    boolean validateMove(int[][] board, int row, int col, int value, int[][] solution);

    int[] getHint(int[][] board, int[][] solution);
}
