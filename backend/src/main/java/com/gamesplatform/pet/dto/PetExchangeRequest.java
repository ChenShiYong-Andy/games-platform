package com.gamesplatform.pet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 宠物权益兑换请求。
 */
@Data
public class PetExchangeRequest {

    /**
     * 权益 ID。
     */
    @NotNull(message = "权益ID不能为空")
    private Long benefitId;
}
