package com.gamesplatform.school.pet.dto;

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

    /**
     * 是否按需批量使用，直到对应状态达到 100%。
     */
    private boolean useAll;
}
