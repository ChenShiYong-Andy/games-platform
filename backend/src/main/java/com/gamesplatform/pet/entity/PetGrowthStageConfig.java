package com.gamesplatform.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 宠物成长阶段配置实体。
 */
@Data
@TableName("pet_growth_stage_config")
public class PetGrowthStageConfig {

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
     * 阶段编号。
     */
    private Integer stageNo;
    /**
     * 阶段名称。
     */
    private String stageName;
    /**
     * 阶段开始等级。
     */
    private Integer minLevel;
    /**
     * 阶段结束等级。
     */
    private Integer maxLevel;
    /**
     * 阶段资源标识。
     */
    private String assetKey;
    /**
     * 预览资源标识。
     */
    private String previewAssetKey;
    /**
     * 阶段说明。
     */
    private String description;
    /**
     * 是否启用。
     */
    private Integer enabled;
    /**
     * 排序值。
     */
    private Integer sortNo;
    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
