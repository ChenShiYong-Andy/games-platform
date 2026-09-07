package com.gamesplatform.games.chess.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** 加入象棋房间请求。 */
@Data
public class ChessJoinRoomRequest {
    /**
     * 房间码。
     */
    @Schema(description = "房间码")
    private String roomCode;
}
