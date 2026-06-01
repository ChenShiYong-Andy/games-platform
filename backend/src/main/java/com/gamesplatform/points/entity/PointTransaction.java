package com.gamesplatform.points.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 积分流水实体。
 */
@Data
@TableName("point_transactions")
public class PointTransaction {

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
     * 积分变动值。
     */
    private Integer amount;
    /**
     * 类型。
     */
    private String type;
    /**
     * 来源业务 ID。
     */
    private Long sourceId;
    /**
     * 描述。
     */
    private String description;
    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}
