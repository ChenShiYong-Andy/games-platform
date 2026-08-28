package com.gamesplatform.gomoku.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 五子棋对局实体。 */
@Data
@TableName("gomoku_games")
public class GomokuGame {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String roomCode;
    private Long blackPlayerId;
    private Long whitePlayerId;
    private Long currentPlayerId;
    private Long winnerId;
    private String boardJson;
    private Integer moveCount;
    private Integer lastMoveRow;
    private Integer lastMoveCol;
    private String status;
    private String finishReason;
    private String gameMode;
    private String humanColor;
    private LocalDateTime createdAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
