package com.gamesplatform.games.gomoku.controller;

import io.swagger.v3.oas.annotations.media.Schema;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.games.gomoku.dto.GomokuGameResponse;
import com.gamesplatform.games.gomoku.dto.JoinRoomRequest;
import com.gamesplatform.games.gomoku.dto.MoveRequest;
import com.gamesplatform.games.gomoku.service.GomokuService;
import com.gamesplatform.games.domain.WaitingRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 双人五子棋接口。 */
@RestController
@RequestMapping("/api/gomoku")
@RequiredArgsConstructor
public class GomokuController {
    /**
     * 五子棋业务服务。
     */
    @Schema(description = "五子棋业务服务")
    private final GomokuService gomokuService;


    /**
     * 创建好友对战邀请房间。
     *
     * @param authentication 当前认证信息
     * @return 操作结果
     */
    @PostMapping("/rooms")
    public ApiResponse<GomokuGameResponse> createRoom(Authentication authentication) {
        return ApiResponse.success(gomokuService.createRoom((Long) authentication.getPrincipal()));
    }

    /**
     * 创建人机对局。
     *
     * @param authentication 当前认证信息
     * @return 操作结果
     */
    @PostMapping("/ai-games")
    public ApiResponse<GomokuGameResponse> createAiGame(Authentication authentication) {
        return ApiResponse.success(gomokuService.createAiGame((Long) authentication.getPrincipal()));
    }

    /**
     * 查询当前可加入的等待房间。
     *
     * @param authentication 当前认证信息
     * @return 操作结果
     */
    @GetMapping("/rooms/waiting")
    public ApiResponse<List<WaitingRoomResponse>> getWaitingRooms(Authentication authentication) {
        return ApiResponse.success(gomokuService.getWaitingRooms((Long) authentication.getPrincipal()));
    }

    /**
     * 通过房间码加入好友对局。
     *
     * @param authentication 当前认证信息
     * @param request 请求参数
     * @return 操作结果
     */
    @PostMapping("/rooms/join")
    public ApiResponse<GomokuGameResponse> joinRoom(Authentication authentication, @RequestBody JoinRoomRequest request) {
        return ApiResponse.success(gomokuService.joinRoom((Long) authentication.getPrincipal(), request.getRoomCode()));
    }

    /**
     * 查询指定游戏的最新状态。
     *
     * @param authentication 当前认证信息
     * @param gameId 游戏标识
     * @return 操作结果
     */
    @GetMapping("/games/{gameId}")
    public ApiResponse<GomokuGameResponse> getGame(Authentication authentication, @PathVariable Long gameId) {
        return ApiResponse.success(gomokuService.getGame((Long) authentication.getPrincipal(), gameId));
    }

    /**
     * 查询当前用户未结束的对局。
     *
     * @param authentication 当前认证信息
     * @return 操作结果
     */
    @GetMapping("/games/active")
    public ApiResponse<GomokuGameResponse> getActiveGame(Authentication authentication) {
        return ApiResponse.success(gomokuService.getActiveGame((Long) authentication.getPrincipal()));
    }

    /**
     * 执行玩家走棋并返回最新对局状态。
     *
     * @param authentication 当前认证信息
     * @param gameId 游戏标识
     * @param request 请求参数
     * @return 操作结果
     */
    @PostMapping("/games/{gameId}/moves")
    public ApiResponse<GomokuGameResponse> move(Authentication authentication, @PathVariable Long gameId,
                                                @RequestBody MoveRequest request) {
        return ApiResponse.success(gomokuService.move((Long) authentication.getPrincipal(), gameId, request));
    }

    /**
     * 认输或取消等待中的房间。
     *
     * @param authentication 当前认证信息
     * @param gameId 游戏标识
     * @return 操作结果
     */
    @PostMapping("/games/{gameId}/surrender")
    public ApiResponse<GomokuGameResponse> surrender(Authentication authentication, @PathVariable Long gameId) {
        return ApiResponse.success(gomokuService.surrender((Long) authentication.getPrincipal(), gameId));
    }
}
