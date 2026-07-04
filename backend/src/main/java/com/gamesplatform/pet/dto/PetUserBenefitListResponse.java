package com.gamesplatform.pet.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 我的宠物权益列表响应。
 */
@Data
@Builder
public class PetUserBenefitListResponse {

    /**
     * 用户权益列表。
     */
    private List<PetUserBenefitResponse> list;
}
