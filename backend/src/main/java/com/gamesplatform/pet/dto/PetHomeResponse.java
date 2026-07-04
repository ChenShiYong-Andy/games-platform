package com.gamesplatform.pet.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 宠物首页响应。
 */
@Data
@Builder
public class PetHomeResponse {

    /**
     * 可用积分。
     */
    private Integer availablePoints;
    /**
     * 宠物信息。
     */
    private PetInfoResponse petInfo;
    /**
     * 权益列表。
     */
    private PetBenefitListResponse benefits;
    /**
     * 我的权益。
     */
    private PetUserBenefitListResponse myBenefits;
}
