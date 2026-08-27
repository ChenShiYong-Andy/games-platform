package com.gamesplatform.gomoku.dto;

import lombok.Data;

/** 加入五子棋房间请求。 */
@Data
public class JoinRoomRequest {
    private String roomCode;
}
