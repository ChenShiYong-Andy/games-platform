package com.gamesplatform.pet.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 宠物权益条目响应。
 */
@Data
@Builder
public class PetBenefitItemResponse {

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
     * 是否已拥有。
     */
    private Boolean owned;
    /**
     * 已拥有数量。
     */
    private Integer quantity;
    /**
     * 库存。
     */
    private Integer stock;
    /**
     * 是否可兑换。
     */
    private Boolean canExchange;
}
