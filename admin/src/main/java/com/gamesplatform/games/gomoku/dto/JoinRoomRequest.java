package com.gamesplatform.games.gomoku.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** 加入五子棋房间请求。 */
@Data
public class JoinRoomRequest {
    /**
     * 房间码。
     */
    @Schema(description = "房间码")
    private String roomCode;
}
