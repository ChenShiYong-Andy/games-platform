package com.gamesplatform.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员宠物成长值调整请求。
 */
@Data
public class AdminPetGrowthAdjustRequest {

    /**
     * 管理密码。
     */
    @NotBlank(message = "管理密码不能为空")
    private String adminPassword;
    /**
     * 扣减成长值。
     */
    @NotNull(message = "扣减成长值不能为空")
    @Min(value = 1, message = "扣减成长值不能小于1")
    @Max(value = 100000, message = "扣减成长值不能大于100000")
    private Integer amount;
}
