package com.gamesplatform.sudoku.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sudoku_games")
public class SudokuGame {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String difficulty;
    private String puzzleJson;
    private String solutionJson;
    private String status;
    private Integer elapsedSeconds;
    private Integer hintsUsed;
    private Integer mistakes;
    private Integer score;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
