package com.gamesplatform.games.domain;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;

/** 等待其他玩家加入的房间摘要。 */
@Data
@Builder
public class WaitingRoomResponse {
    /**
     * 唯一标识。
     */
    @Schema(description = "唯一标识")
    private Long id;
    /**
     * 房间码。
     */
    @Schema(description = "房间码")
    private String roomCode;
    /**
     * 房主昵称。
     */
    @Schema(description = "房主昵称")
    private String hostName;
    /**
     * 房主用户名。
     */
    @Schema(description = "房主用户名")
    private String hostUsername;
}
