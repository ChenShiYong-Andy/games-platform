package com.gamesplatform.chess.dto;

import lombok.Data;

/** 加入象棋房间请求。 */
@Data
public class ChessJoinRoomRequest {
    private String roomCode;
}
