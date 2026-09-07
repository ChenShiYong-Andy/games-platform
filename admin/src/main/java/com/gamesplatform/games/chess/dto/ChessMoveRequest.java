package com.gamesplatform.games.chess.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** 象棋走子请求。 */
@Data
public class ChessMoveRequest {
    /**
     * 起始行坐标。
     */
    @Schema(description = "起始行坐标")
    private Integer fromRow;
    /**
     * 起始列坐标。
     */
    @Schema(description = "起始列坐标")
    private Integer fromCol;
    /**
     * 目标行坐标。
     */
    @Schema(description = "目标行坐标")
    private Integer toRow;
    /**
     * 目标列坐标。
     */
    @Schema(description = "目标列坐标")
    private Integer toCol;
}
