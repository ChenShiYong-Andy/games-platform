package com.gamesplatform.pet.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 宠物权益兑换订单实体。
 */
@Data
@TableName("pet_benefit_exchange_order")
public class PetBenefitExchangeOrder {

    /**
     * 主键 ID。
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    /**
     * 订单号。
     */
    private String orderNo;
    /**
     * 用户 ID。
     */
    private Long userId;
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
     * 消耗积分。
     */
    private Integer costPoints;
    /**
     * 兑换数量。
     */
    private Integer quantity;
    /**
     * 状态。
     */
    private Integer status;
    /**
     * 失败原因。
     */
    private String failReason;
    /**
     * 创建时间。
     */
    private LocalDateTime createTime;
    /**
     * 更新时间。
     */
    private LocalDateTime updateTime;
}
