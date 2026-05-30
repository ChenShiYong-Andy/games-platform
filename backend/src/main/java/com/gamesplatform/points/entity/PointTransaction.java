package com.gamesplatform.points.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("point_transactions")
public class PointTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer amount;
    private String type;
    private Long sourceId;
    private String description;
    private LocalDateTime createdAt;
}
