package com.gamesplatform.gomoku.dto;

import lombok.Data;

/** 五子棋落子请求。 */
@Data
public class MoveRequest {
    private Integer row;
    private Integer col;
}
