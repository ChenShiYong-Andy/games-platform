package com.gamesplatform.sudoku.dto;

import lombok.Data;

/**
 * 数独游戏创建请求。
 */
@Data
public class CreateGameRequest {

    /**
     * 游戏难度。
     */
    private String difficulty;
}
