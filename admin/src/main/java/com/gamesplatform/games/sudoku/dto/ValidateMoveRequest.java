package com.gamesplatform.games.sudoku.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 数独落子校验请求。
 */
@Data
public class ValidateMoveRequest {
    /**
     * 行索引。
     */
    @Schema(description = "行索引")
    private int row;
    /**
     * 列索引。
     */
    @Schema(description = "列索引")
    private int col;
    /**
     * 填入值。
     */
    @Schema(description = "填入值")
    private int value;
}
