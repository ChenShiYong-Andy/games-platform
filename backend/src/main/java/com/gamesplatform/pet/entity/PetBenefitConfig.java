package com.gamesplatform.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 宠物权益配置实体。
 */
@Data
@TableName("pet_benefit_config")
public class PetBenefitConfig {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 权益编码。
     */
    private String benefitCode;
    /**
     * 权益名称。
     */
    private String benefitName;
    /**
     * 权益类型。
     */
    private String benefitType;
    /**
     * 消耗积分。
     */
    private Integer costPoints;
    /**
     * 权益说明。
     */
    private String description;
    /**
     * 效果类型。
     */
    private String effectType;
    /**
     * 效果值。
     */
    private Integer effectValue;
    /**
     * 图标地址。
     */
    private String iconUrl;
    /**
     * 库存。
     */
    private Integer stock;
    /**
     * 每日兑换限制。
     */
    private Integer exchangeLimitDay;
    /**
     * 总兑换限制。
     */
    private Integer exchangeLimitTotal;
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
