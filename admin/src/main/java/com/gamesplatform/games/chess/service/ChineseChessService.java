package com.gamesplatform.games.chess.service;

import com.gamesplatform.games.chess.dto.ChessMoveRequest;
import com.gamesplatform.games.chess.dto.ChineseChessGameResponse;
import com.gamesplatform.games.domain.WaitingRoomResponse;

import java.util.List;

/**
 * 中国象棋业务服务。
 */
public interface ChineseChessService {

    /**
     * 创建好友对局房间。
     *
     * @param userId 当前用户 ID。
     * @return 创建后的对局信息。
     */
    ChineseChessGameResponse createRoom(Long userId);

    /**
     * 创建人机对局。
     *
     * @param userId 当前用户 ID。
     * @return 创建后的对局信息。
     */
    ChineseChessGameResponse createAiGame(Long userId);

    /**
     * 加入指定好友对局房间。
     *
     * @param userId 当前用户 ID。
     * @param rawCode 原始房间码。
     * @return 加入后的对局信息。
     */
    ChineseChessGameResponse joinRoom(Long userId, String rawCode);

    /**
     * 查询指定对局。
     *
     * @param userId 当前用户 ID。
     * @param gameId 对局 ID。
     * @return 对局信息。
     */
    ChineseChessGameResponse getGame(Long userId, Long gameId);

    /**
     * 查询当前用户进行中的对局。
     *
     * @param userId 当前用户 ID。
     * @return 进行中的对局，不存在时返回 {@code null}。
     */
    ChineseChessGameResponse getActiveGame(Long userId);

    /**
     * 查询等待加入的房间。
     *
     * @param userId 当前用户 ID。
     * @return 等待中的房间列表。
     */
    List<WaitingRoomResponse> getWaitingRooms(Long userId);

    /**
     * 执行一步走棋。
     *
     * @param userId 当前用户 ID。
     * @param gameId 对局 ID。
     * @param request 走棋请求。
     * @return 更新后的对局信息。
     */
    ChineseChessGameResponse move(Long userId, Long gameId, ChessMoveRequest request);

    /**
     * 认输并结束对局。
     *
     * @param userId 当前用户 ID。
     * @param gameId 对局 ID。
     * @return 结束后的对局信息。
     */
    ChineseChessGameResponse surrender(Long userId, Long gameId);
}
