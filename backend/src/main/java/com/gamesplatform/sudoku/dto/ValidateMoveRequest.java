package com.gamesplatform.sudoku.dto;

import lombok.Data;

/**
 * 数独落子校验请求。
 */
@Data
public class ValidateMoveRequest {
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
