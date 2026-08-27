package com.gamesplatform.chess.dto;

import lombok.Data;

/** 象棋走子请求。 */
@Data
public class ChessMoveRequest {
    private Integer fromRow;
    private Integer fromCol;
    private Integer toRow;
    private Integer toCol;
}
