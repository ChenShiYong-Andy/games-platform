package com.gamesplatform.games.sudoku.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.Data;

/**
 * 数独游戏创建请求。
 */
@Data
public class CreateGameRequest {

    /**
     * 游戏难度。
     */
    @Schema(description = "游戏难度")
    private String difficulty;
}
