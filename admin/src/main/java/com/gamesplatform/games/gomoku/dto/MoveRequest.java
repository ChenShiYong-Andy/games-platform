package com.gamesplatform.games.gomoku.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/** 五子棋落子请求。 */
@Data
public class MoveRequest {
    /**
     * 棋盘行坐标。
     */
    @Schema(description = "棋盘行坐标")
    private Integer row;
    /**
     * 棋盘列坐标。
     */
    @Schema(description = "棋盘列坐标")
    private Integer col;
}
