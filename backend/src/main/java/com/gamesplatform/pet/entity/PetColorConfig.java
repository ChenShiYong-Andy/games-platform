package com.gamesplatform.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 宠物颜色配置实体。
 */
@Data
@TableName("pet_color_config")
public class PetColorConfig {

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
     * 颜色编码。
     */
    private String colorCode;
    /**
     * 颜色名称。
     */
    private String colorName;
    /**
     * 颜色值。
     */
    private String colorHex;
    /**
     * 基础资源标识。
     */
    private String assetKey;
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
