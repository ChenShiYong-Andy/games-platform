package com.gamesplatform.games.engine;

import com.gamesplatform.games.domain.GameResult;
import com.gamesplatform.games.domain.GameSession;
import com.gamesplatform.games.domain.GameSubmitCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SudokuGameEngineTest {

    private final SudokuGameEngine engine = new SudokuGameEngine(new SudokuGenerator());

    @Test
    void awardsPointsByDifficulty() {
        assertPoints("EASY", 3);
        assertPoints("MEDIUM", 8);
        assertPoints("HARD", 20);
    }

    private void assertPoints(String difficulty, int expectedPoints) {
        GameSession session = engine.createGame(difficulty);
        GameSubmitCommand command = new GameSubmitCommand();
        command.setBoard(session.getSolution());

        GameResult result = engine.submitWithSolution(command, session.getSolution(), difficulty);

        assertTrue(result.isSuccess());
        assertEquals(expectedPoints, result.getPointsEarned());
    }
}
