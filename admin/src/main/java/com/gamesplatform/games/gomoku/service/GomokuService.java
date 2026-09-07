package com.gamesplatform.games.gomoku.service;

import com.gamesplatform.games.domain.WaitingRoomResponse;
import com.gamesplatform.games.gomoku.dto.GomokuGameResponse;
import com.gamesplatform.games.gomoku.dto.MoveRequest;

import java.util.List;

/**
 * 五子棋业务服务。
 */
public interface GomokuService {

    /**
     * 创建好友对局房间。
     *
     * @param userId 当前用户 ID。
     * @return 创建后的对局信息。
     */
    GomokuGameResponse createRoom(Long userId);

    /**
     * 创建人机对局。
     *
     * @param userId 当前用户 ID。
     * @return 创建后的对局信息。
     */
    GomokuGameResponse createAiGame(Long userId);

    /**
     * 加入指定好友对局房间。
     *
     * @param userId 当前用户 ID。
     * @param rawRoomCode 原始房间码。
     * @return 加入后的对局信息。
     */
    GomokuGameResponse joinRoom(Long userId, String rawRoomCode);

    /**
     * 查询指定对局。
     *
     * @param userId 当前用户 ID。
     * @param gameId 对局 ID。
     * @return 对局信息。
     */
    GomokuGameResponse getGame(Long userId, Long gameId);

    /**
     * 查询当前用户进行中的对局。
     *
     * @param userId 当前用户 ID。
     * @return 进行中的对局，不存在时返回 {@code null}。
     */
    GomokuGameResponse getActiveGame(Long userId);

    /**
     * 查询等待加入的房间。
     *
     * @param userId 当前用户 ID。
     * @return 等待中的房间列表。
     */
    List<WaitingRoomResponse> getWaitingRooms(Long userId);

    /**
     * 执行一步落子。
     *
     * @param userId 当前用户 ID。
     * @param gameId 对局 ID。
     * @param request 落子请求。
     * @return 更新后的对局信息。
     */
    GomokuGameResponse move(Long userId, Long gameId, MoveRequest request);

    /**
     * 认输并结束对局。
     *
     * @param userId 当前用户 ID。
     * @param gameId 对局 ID。
     * @return 结束后的对局信息。
     */
    GomokuGameResponse surrender(Long userId, Long gameId);
}
