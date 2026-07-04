package com.gamesplatform.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 宠物权益使用日志实体。
 */
@Data
@TableName("pet_benefit_use_log")
public class PetBenefitUseLog {

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
     * 用户权益 ID。
     */
    private Long userBenefitId;
    /**
     * 权益 ID。
     */
    private Long benefitId;
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
     * 效果类型。
     */
    private String effectType;
    /**
     * 效果值。
     */
    private Integer effectValue;
    /**
     * 效果描述。
     */
    private String effectDesc;
    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
}
