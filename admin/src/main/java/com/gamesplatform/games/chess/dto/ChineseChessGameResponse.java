package com.gamesplatform.games.chess.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;

/** 中国象棋对局响应。 */
@Data
@Builder
public class ChineseChessGameResponse {
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
     * 红方玩家标识。
     */
    @Schema(description = "红方玩家标识")
    private Long redPlayerId;
    /**
     * 红方玩家昵称。
     */
    @Schema(description = "红方玩家昵称")
    private String redPlayerName;
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
     * 当前用户是否被将军。
     */
    @Schema(description = "当前用户是否被将军")
    private boolean inCheck;
    /**
     * 最后一步起始行坐标。
     */
    @Schema(description = "最后一步起始行坐标")
    private Integer lastFromRow;
    /**
     * 最后一步起始列坐标。
     */
    @Schema(description = "最后一步起始列坐标")
    private Integer lastFromCol;
    /**
     * 最后一步目标行坐标。
     */
    @Schema(description = "最后一步目标行坐标")
    private Integer lastToRow;
    /**
     * 最后一步目标列坐标。
     */
    @Schema(description = "最后一步目标列坐标")
    private Integer lastToCol;
    /**
     * 本局获得积分。
     */
    @Schema(description = "本局获得积分")
    private Integer pointsEarned;
}
