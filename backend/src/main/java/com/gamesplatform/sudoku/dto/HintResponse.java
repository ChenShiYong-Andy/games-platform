package com.gamesplatform.sudoku.dto;

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
    private int row;
    /**
     * 列索引。
     */
    private int col;
    /**
     * 填入值。
     */
    private int value;
}
