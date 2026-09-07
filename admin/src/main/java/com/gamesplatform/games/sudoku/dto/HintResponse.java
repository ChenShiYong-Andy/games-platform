package com.gamesplatform.games.sudoku.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;

/**
 * 数独提示响应。
 */
@Data
@Builder
public class HintResponse {
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
