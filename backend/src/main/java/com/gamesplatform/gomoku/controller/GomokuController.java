package com.gamesplatform.gomoku.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.gomoku.dto.GomokuGameResponse;
import com.gamesplatform.gomoku.dto.JoinRoomRequest;
import com.gamesplatform.gomoku.dto.MoveRequest;
import com.gamesplatform.gomoku.service.GomokuService;
import com.gamesplatform.game.domain.WaitingRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 双人五子棋接口。 */
@RestController
@RequestMapping("/api/gomoku")
@RequiredArgsConstructor
public class GomokuController {
    private final GomokuService gomokuService;

    @PostMapping("/rooms")
    public ApiResponse<GomokuGameResponse> createRoom(Authentication authentication) {
        return ApiResponse.success(gomokuService.createRoom((Long) authentication.getPrincipal()));
    }

    @GetMapping("/rooms/waiting")
    public ApiResponse<List<WaitingRoomResponse>> getWaitingRooms(Authentication authentication) {
        return ApiResponse.success(gomokuService.getWaitingRooms((Long) authentication.getPrincipal()));
    }

    @PostMapping("/rooms/join")
    public ApiResponse<GomokuGameResponse> joinRoom(Authentication authentication, @RequestBody JoinRoomRequest request) {
        return ApiResponse.success(gomokuService.joinRoom((Long) authentication.getPrincipal(), request.getRoomCode()));
    }

    @GetMapping("/games/{gameId}")
    public ApiResponse<GomokuGameResponse> getGame(Authentication authentication, @PathVariable Long gameId) {
        return ApiResponse.success(gomokuService.getGame((Long) authentication.getPrincipal(), gameId));
    }

    @GetMapping("/games/active")
    public ApiResponse<GomokuGameResponse> getActiveGame(Authentication authentication) {
        return ApiResponse.success(gomokuService.getActiveGame((Long) authentication.getPrincipal()));
    }

    @PostMapping("/games/{gameId}/moves")
    public ApiResponse<GomokuGameResponse> move(Authentication authentication, @PathVariable Long gameId,
                                                @RequestBody MoveRequest request) {
        return ApiResponse.success(gomokuService.move((Long) authentication.getPrincipal(), gameId, request));
    }

    @PostMapping("/games/{gameId}/surrender")
    public ApiResponse<GomokuGameResponse> surrender(Authentication authentication, @PathVariable Long gameId) {
        return ApiResponse.success(gomokuService.surrender((Long) authentication.getPrincipal(), gameId));
    }
}
