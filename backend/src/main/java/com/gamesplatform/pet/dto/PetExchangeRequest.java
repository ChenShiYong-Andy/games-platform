package com.gamesplatform.pet.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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
    /**
     * 兑换数量。
     */
    @Min(value = 1, message = "兑换数量不能小于1")
    @Max(value = 100, message = "兑换数量不能大于100")
    private Integer quantity;
}
