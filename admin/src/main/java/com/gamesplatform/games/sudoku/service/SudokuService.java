package com.gamesplatform.games.sudoku.service;

import com.gamesplatform.games.sudoku.dto.GameResponse;
import com.gamesplatform.games.sudoku.dto.SubmitGameRequest;
import com.gamesplatform.games.sudoku.dto.SubmitGameResponse;
import com.gamesplatform.games.sudoku.dto.ValidateMoveRequest;
import com.gamesplatform.games.sudoku.dto.ValidateMoveResponse;

import java.util.List;

/**
 * 数独业务服务。
 */
public interface SudokuService {

    /**
     * 创建指定难度的数独游戏。
     *
     * @param userId 当前用户 ID。
     * @param difficulty 难度标识。
     * @return 创建后的游戏信息。
     */
    GameResponse createGame(Long userId, String difficulty);

    /**
     * 查询指定数独游戏。
     *
     * @param userId 当前用户 ID。
     * @param gameId 游戏 ID。
     * @return 游戏信息。
     */
    GameResponse getGame(Long userId, Long gameId);

    /**
     * 查询用户的数独游戏历史。
     *
     * @param userId 当前用户 ID。
     * @param limit 返回数量上限。
     * @return 游戏历史列表。
     */
    List<GameResponse> getGameHistory(Long userId, int limit);

    /**
     * 校验一次数独填数。
     *
     * @param userId 当前用户 ID。
     * @param gameId 游戏 ID。
     * @param request 填数请求。
     * @return 校验结果。
     */
    ValidateMoveResponse validateMove(Long userId, Long gameId, ValidateMoveRequest request);

    /**
     * 提交并结算数独游戏。
     *
     * @param userId 当前用户 ID。
     * @param gameId 游戏 ID。
     * @param request 提交请求。
     * @return 结算结果。
     */
    SubmitGameResponse submitGame(Long userId, Long gameId, SubmitGameRequest request);
}
