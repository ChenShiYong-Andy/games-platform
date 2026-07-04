package com.gamesplatform.pet.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 用户宠物权益响应。
 */
@Data
@Builder
public class PetUserBenefitResponse {

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
     * 数量。
     */
    private Integer quantity;
    /**
     * 状态。
     */
    private Integer status;
}
