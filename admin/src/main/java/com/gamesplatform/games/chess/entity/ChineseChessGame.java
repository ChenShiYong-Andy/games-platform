package com.gamesplatform.games.chess.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 中国象棋对局实体。 */
@Data
@TableName("chinese_chess_games")
public class ChineseChessGame {
    /**
     * 唯一标识。
     */
    @Schema(description = "唯一标识")
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 房间码。
     */
    @Schema(description = "房间码")
    private String roomCode;
    /**
     * 红方玩家标识。
     */
    @Schema(description = "红方玩家标识")
    private Long redPlayerId;
    /**
     * 黑方玩家标识。
     */
    @Schema(description = "黑方玩家标识")
    private Long blackPlayerId;
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
     * 棋盘数据 JSON。
     */
    @Schema(description = "棋盘数据 JSON")
    private String boardJson;
    /**
     * 已走棋步数。
     */
    @Schema(description = "已走棋步数")
    private Integer moveCount;
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
     * 人机对局中玩家执棋颜色。
     */
    @Schema(description = "人机对局中玩家执棋颜色")
    private String humanColor;
    /**
     * 创建时间。
     */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    /**
     * 开始时间。
     */
    @Schema(description = "开始时间")
    private LocalDateTime startedAt;
    /**
     * 完成时间。
     */
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
}
