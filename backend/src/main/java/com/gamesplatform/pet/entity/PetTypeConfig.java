package com.gamesplatform.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 宠物类型配置实体。
 */
@Data
@TableName("pet_type_config")
public class PetTypeConfig {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 宠物类型编码。
     */
    private String petType;
    /**
     * 宠物类型名称。
     */
    private String petName;
    /**
     * 说明。
     */
    private String description;
    /**
     * 默认颜色编码。
     */
    private String defaultColorCode;
    /**
     * 排序值。
     */
    private Integer sortNo;
    /**
     * 是否启用。
     */
    private Integer enabled;
    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
