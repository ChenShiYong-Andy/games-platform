package com.gamesplatform.games.chess.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import com.gamesplatform.games.chess.dto.ChessJoinRoomRequest;
import com.gamesplatform.games.chess.dto.ChessMoveRequest;
import com.gamesplatform.games.chess.dto.ChineseChessGameResponse;
import com.gamesplatform.games.chess.service.ChineseChessService;
import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.games.domain.WaitingRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 双人中国象棋接口。 */
@RestController
@RequestMapping("/api/chess")
@RequiredArgsConstructor
public class ChineseChessController {
    /**
     * 中国象棋业务服务。
     */
    @Schema(description = "中国象棋业务服务")
    private final ChineseChessService chessService;


    /**
     * 创建好友对战邀请房间。
     *
     * @param auth 当前认证信息
     * @return 操作结果
     */
    @PostMapping("/rooms")
    public ApiResponse<ChineseChessGameResponse> createRoom(Authentication auth) {
        return ApiResponse.success(chessService.createRoom((Long) auth.getPrincipal()));
    }

    /**
     * 创建人机对局。
     *
     * @param auth 当前认证信息
     * @return 操作结果
     */
    @PostMapping("/ai-games")
    public ApiResponse<ChineseChessGameResponse> createAiGame(Authentication auth) {
        return ApiResponse.success(chessService.createAiGame((Long) auth.getPrincipal()));
    }

    /**
     * 查询当前可加入的等待房间。
     *
     * @param auth 当前认证信息
     * @return 操作结果
     */
    @GetMapping("/rooms/waiting")
    public ApiResponse<List<WaitingRoomResponse>> getWaitingRooms(Authentication auth) {
        return ApiResponse.success(chessService.getWaitingRooms((Long) auth.getPrincipal()));
    }

    /**
     * 通过房间码加入好友对局。
     *
     * @param auth 当前认证信息
     * @param request 请求参数
     * @return 操作结果
     */
    @PostMapping("/rooms/join")
    public ApiResponse<ChineseChessGameResponse> joinRoom(Authentication auth, @RequestBody ChessJoinRoomRequest request) {
        return ApiResponse.success(chessService.joinRoom((Long) auth.getPrincipal(), request.getRoomCode()));
    }

    /**
     * 查询当前用户未结束的对局。
     *
     * @param auth 当前认证信息
     * @return 操作结果
     */
    @GetMapping("/games/active")
    public ApiResponse<ChineseChessGameResponse> getActiveGame(Authentication auth) {
        return ApiResponse.success(chessService.getActiveGame((Long) auth.getPrincipal()));
    }

    /**
     * 查询指定游戏的最新状态。
     *
     * @param auth 当前认证信息
     * @param gameId 游戏标识
     * @return 操作结果
     */
    @GetMapping("/games/{gameId}")
    public ApiResponse<ChineseChessGameResponse> getGame(Authentication auth, @PathVariable Long gameId) {
        return ApiResponse.success(chessService.getGame((Long) auth.getPrincipal(), gameId));
    }

    /**
     * 执行玩家走棋并返回最新对局状态。
     *
     * @param auth 当前认证信息
     * @param gameId 游戏标识
     * @param request 请求参数
     * @return 操作结果
     */
    @PostMapping("/games/{gameId}/moves")
    public ApiResponse<ChineseChessGameResponse> move(Authentication auth, @PathVariable Long gameId,
                                                      @RequestBody ChessMoveRequest request) {
        return ApiResponse.success(chessService.move((Long) auth.getPrincipal(), gameId, request));
    }

    /**
     * 认输或取消等待中的房间。
     *
     * @param auth 当前认证信息
     * @param gameId 游戏标识
     * @return 操作结果
     */
    @PostMapping("/games/{gameId}/surrender")
    public ApiResponse<ChineseChessGameResponse> surrender(Authentication auth, @PathVariable Long gameId) {
        return ApiResponse.success(chessService.surrender((Long) auth.getPrincipal(), gameId));
    }
}
