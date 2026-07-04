package com.gamesplatform.pet.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 宠物权益列表响应。
 */
@Data
@Builder
public class PetBenefitListResponse {

    /**
     * 当前可用积分。
     */
    private Integer availablePoints;
    /**
     * 权益列表。
     */
    private List<PetBenefitItemResponse> list;
}
