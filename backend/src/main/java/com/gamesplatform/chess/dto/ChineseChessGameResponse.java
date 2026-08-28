package com.gamesplatform.chess.dto;

import lombok.Builder;
import lombok.Data;

/** 中国象棋对局响应。 */
@Data
@Builder
public class ChineseChessGameResponse {
    private Long id;
    private String roomCode;
    private int[][] board;
    private String status;
    private String finishReason;
    private String gameMode;
    private Integer moveCount;
    private Long redPlayerId;
    private String redPlayerName;
    private Long blackPlayerId;
    private String blackPlayerName;
    private Long currentPlayerId;
    private Long winnerId;
    private String myColor;
    private boolean myTurn;
    private boolean inCheck;
    private Integer lastFromRow;
    private Integer lastFromCol;
    private Integer lastToRow;
    private Integer lastToCol;
    private Integer pointsEarned;
}
