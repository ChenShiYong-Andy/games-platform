package com.gamesplatform.games.sudoku.entity;

import io.swagger.v3.oas.annotations.media.Schema;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数独游戏记录实体。
 */
@Data
@TableName("sudoku_games")
public class SudokuGame {

    /**
     * 主键 ID。
     */
    @Schema(description = "主键 ID")
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 用户 ID。
     */
    @Schema(description = "用户 ID")
    private Long userId;
    /**
     * 游戏难度。
     */
    @Schema(description = "游戏难度")
    private String difficulty;
    /**
     * 题目棋盘 JSON。
     */
    @Schema(description = "题目棋盘 JSON")
    private String puzzleJson;
    /**
     * 答案棋盘 JSON。
     */
    @Schema(description = "答案棋盘 JSON")
    private String solutionJson;
    /**
     * 状态。
     */
    @Schema(description = "状态")
    private String status;
    /**
     * 耗时秒数。
     */
    @Schema(description = "耗时秒数")
    private Integer elapsedSeconds;
    /**
     * 已使用提示次数。
     */
    @Schema(description = "已使用提示次数")
    private Integer hintsUsed;
    /**
     * 错误次数。
     */
    @Schema(description = "错误次数")
    private Integer mistakes;
    /**
     * 得分。
     */
    @Schema(description = "得分")
    private Integer score;
    /**
     * 开始时间。
     */
    @Schema(description = "开始时间")
    private LocalDateTime startedAt;
    /**
     * 完成时间。
     */
    @Schema(description = "完成时间")
    private LocalDateTime completedAt;
}
