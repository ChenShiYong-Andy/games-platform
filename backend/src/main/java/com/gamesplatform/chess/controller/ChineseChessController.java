package com.gamesplatform.chess.controller;

import com.gamesplatform.chess.dto.ChessJoinRoomRequest;
import com.gamesplatform.chess.dto.ChessMoveRequest;
import com.gamesplatform.chess.dto.ChineseChessGameResponse;
import com.gamesplatform.chess.service.ChineseChessService;
import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.game.domain.WaitingRoomResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 双人中国象棋接口。 */
@RestController
@RequestMapping("/api/chess")
@RequiredArgsConstructor
public class ChineseChessController {
    private final ChineseChessService chessService;

    @PostMapping("/rooms")
    public ApiResponse<ChineseChessGameResponse> createRoom(Authentication auth) {
        return ApiResponse.success(chessService.createRoom((Long) auth.getPrincipal()));
    }

    @GetMapping("/rooms/waiting")
    public ApiResponse<List<WaitingRoomResponse>> getWaitingRooms(Authentication auth) {
        return ApiResponse.success(chessService.getWaitingRooms((Long) auth.getPrincipal()));
    }

    @PostMapping("/rooms/join")
    public ApiResponse<ChineseChessGameResponse> joinRoom(Authentication auth, @RequestBody ChessJoinRoomRequest request) {
        return ApiResponse.success(chessService.joinRoom((Long) auth.getPrincipal(), request.getRoomCode()));
    }

    @GetMapping("/games/active")
    public ApiResponse<ChineseChessGameResponse> getActiveGame(Authentication auth) {
        return ApiResponse.success(chessService.getActiveGame((Long) auth.getPrincipal()));
    }

    @GetMapping("/games/{gameId}")
    public ApiResponse<ChineseChessGameResponse> getGame(Authentication auth, @PathVariable Long gameId) {
        return ApiResponse.success(chessService.getGame((Long) auth.getPrincipal(), gameId));
    }

    @PostMapping("/games/{gameId}/moves")
    public ApiResponse<ChineseChessGameResponse> move(Authentication auth, @PathVariable Long gameId,
                                                      @RequestBody ChessMoveRequest request) {
        return ApiResponse.success(chessService.move((Long) auth.getPrincipal(), gameId, request));
    }

    @PostMapping("/games/{gameId}/surrender")
    public ApiResponse<ChineseChessGameResponse> surrender(Authentication auth, @PathVariable Long gameId) {
        return ApiResponse.success(chessService.surrender((Long) auth.getPrincipal(), gameId));
    }
}
