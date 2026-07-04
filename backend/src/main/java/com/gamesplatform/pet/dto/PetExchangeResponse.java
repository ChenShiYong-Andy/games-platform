package com.gamesplatform.pet.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 宠物权益兑换响应。
 */
@Data
@Builder
public class PetExchangeResponse {

    /**
     * 订单号。
     */
    private String orderNo;
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
     * 可用积分。
     */
    private Integer availablePoints;
    /**
     * 当前数量。
     */
    private Integer quantity;
}
