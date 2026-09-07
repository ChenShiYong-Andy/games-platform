package com.gamesplatform.games.sudoku.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Builder;
import lombok.Data;

/**
 * 数独落子校验响应。
 */
@Data
@Builder
public class ValidateMoveResponse {
    /**
     * 落子是否合法。
     */
    @Schema(description = "落子是否合法")
    private boolean valid;
}
