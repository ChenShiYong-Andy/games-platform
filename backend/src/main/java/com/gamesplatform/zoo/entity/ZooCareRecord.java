package com.gamesplatform.zoo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 动物园照料记录实体。
 */
@Data
@TableName("zoo_care_records")
public class ZooCareRecord {

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
     * 照料时间。
     */
    private LocalDateTime caredAt;
}
