package com.gamesplatform.zoo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动物园游戏记录实体。
 */
@Data
@TableName("zoo_games")
public class ZooGame {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 用户 ID。
     */
    private Long userId;
    /**
     * 状态。
     */
    private String status;
    /**
     * 当前照料需求。
     */
    private String currentNeed;
    /**
     * 得分。
     */
    private Integer score;
    /**
     * 已获得积分。
     */
    private Integer pointsAwarded;
    /**
     * 已扣除积分。
     */
    private Integer pointsDeducted;
    /**
     * 开始时间。
     */
    private LocalDateTime startedAt;
    /**
     * 完成时间。
     */
    private LocalDateTime completedAt;
}
