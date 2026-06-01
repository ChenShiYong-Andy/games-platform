package com.gamesplatform.game.engine;

import com.gamesplatform.game.domain.GameResult;
import com.gamesplatform.game.domain.GameSession;
import com.gamesplatform.game.domain.GameSubmitCommand;

/**
 * 游戏引擎接口。
 */
public interface GameEngine {

    /**
     * 查询游戏类型。
     *
     * @return 处理结果。
     */
    String getGameType();

    /**
     * 创建游戏。
     *
     * @param difficulty 游戏难度。
     * @return 处理结果。
     */
    GameSession createGame(String difficulty);

    /**
     * 提交游戏结果。
     *
     * @param command 游戏提交命令。
     * @return 处理结果。
     */
    GameResult submit(GameSubmitCommand command);

    /**
     * 校验落子。
     *
     * @param board 当前棋盘。
     * @param row 行索引。
     * @param col 列索引。
     * @param value 填入值。
     * @param solution 答案棋盘。
     * @return 处理结果。
     */
    boolean validateMove(int[][] board, int row, int col, int value, int[][] solution);

    /**
     * 获取提示。
     *
     * @param board 当前棋盘。
     * @param solution 答案棋盘。
     * @return 处理结果。
     */
    int[] getHint(int[][] board, int[][] solution);
}
