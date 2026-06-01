package com.gamesplatform.sudoku.dto;

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
    private boolean valid;
}
