package com.gamesplatform.game.domain;

import lombok.Builder;
import lombok.Data;

/** 等待其他玩家加入的房间摘要。 */
@Data
@Builder
public class WaitingRoomResponse {
    private Long id;
    private String roomCode;
    private String hostName;
    private String hostUsername;
}
