package com.gamesplatform.chess.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 中国象棋对局实体。 */
@Data
@TableName("chinese_chess_games")
public class ChineseChessGame {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roomCode;
    private Long redPlayerId;
    private Long blackPlayerId;
    private Long currentPlayerId;
    private Long winnerId;
    private String boardJson;
    private Integer moveCount;
    private Integer lastFromRow;
    private Integer lastFromCol;
    private Integer lastToRow;
    private Integer lastToCol;
    private String status;
    private String finishReason;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
