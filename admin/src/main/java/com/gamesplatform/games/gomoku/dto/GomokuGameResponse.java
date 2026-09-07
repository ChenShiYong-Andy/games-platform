package com.gamesplatform.games.gomoku.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;

/** 五子棋对局响应。 */
@Data
@Builder
public class GomokuGameResponse {
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
     * 当前棋盘数据。
     */
    @Schema(description = "当前棋盘数据")
    private int[][] board;
    /**
     * 游戏状态。
     */
    @Schema(description = "游戏状态")
    private String status;
    /**
     * 对局结束原因。
     */
    @Schema(description = "对局结束原因")
    private String finishReason;
    /**
     * 对局模式。
     */
    @Schema(description = "对局模式")
    private String gameMode;
    /**
     * 已走棋步数。
     */
    @Schema(description = "已走棋步数")
    private Integer moveCount;
    /**
     * 最后落子的行坐标。
     */
    @Schema(description = "最后落子的行坐标")
    private Integer lastMoveRow;
    /**
     * 最后落子的列坐标。
     */
    @Schema(description = "最后落子的列坐标")
    private Integer lastMoveCol;
    /**
     * 黑方玩家标识。
     */
    @Schema(description = "黑方玩家标识")
    private Long blackPlayerId;
    /**
     * 黑方玩家昵称。
     */
    @Schema(description = "黑方玩家昵称")
    private String blackPlayerName;
    /**
     * 白方玩家标识。
     */
    @Schema(description = "白方玩家标识")
    private Long whitePlayerId;
    /**
     * 白方玩家昵称。
     */
    @Schema(description = "白方玩家昵称")
    private String whitePlayerName;
    /**
     * 当前行动玩家标识。
     */
    @Schema(description = "当前行动玩家标识")
    private Long currentPlayerId;
    /**
     * 获胜玩家标识。
     */
    @Schema(description = "获胜玩家标识")
    private Long winnerId;
    /**
     * 当前用户执棋颜色。
     */
    @Schema(description = "当前用户执棋颜色")
    private String myColor;
    /**
     * 是否轮到当前用户行动。
     */
    @Schema(description = "是否轮到当前用户行动")
    private boolean myTurn;
    /**
     * 本局获得积分。
     */
    @Schema(description = "本局获得积分")
    private Integer pointsEarned;
}
