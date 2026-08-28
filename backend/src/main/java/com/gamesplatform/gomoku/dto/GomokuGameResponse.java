package com.gamesplatform.gomoku.dto;

import lombok.Builder;
import lombok.Data;

/** 五子棋对局响应。 */
@Data
@Builder
public class GomokuGameResponse {
    private Long id;
    private String roomCode;
    private int[][] board;
    private String status;
    private String finishReason;
    private String gameMode;
    private Integer moveCount;
    private Integer lastMoveRow;
    private Integer lastMoveCol;
    private Long blackPlayerId;
    private String blackPlayerName;
    private Long whitePlayerId;
    private String whitePlayerName;
    private Long currentPlayerId;
    private Long winnerId;
    private String myColor;
    private boolean myTurn;
    private Integer pointsEarned;
}
