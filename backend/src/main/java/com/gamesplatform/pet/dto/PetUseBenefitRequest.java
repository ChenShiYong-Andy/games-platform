package com.gamesplatform.pet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 宠物权益使用请求。
 */
@Data
public class PetUseBenefitRequest {

    /**
     * 用户权益 ID。
     */
    @NotNull(message = "用户权益ID不能为空")
    private Long userBenefitId;
}
