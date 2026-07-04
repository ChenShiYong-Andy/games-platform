package com.gamesplatform.pet.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 我的宠物资料响应。
 */
@Data
@Builder
public class PetProfileResponse {

    /**
     * 是否已领养宠物。
     */
    private Boolean hasPet;
    /**
     * 宠物信息。
     */
    private PetInfoResponse petInfo;
}
