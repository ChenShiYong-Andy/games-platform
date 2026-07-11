package com.gamesplatform.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员积分调整请求。
 */
@Data
public class AdminPointsAdjustRequest {

    /**
     * 管理密码。
     */
    @NotBlank(message = "管理密码不能为空")
    private String adminPassword;
    /**
     * 积分调整值，正数增加，负数扣减。
     */
    @NotNull(message = "积分调整值不能为空")
    @Min(value = -100000, message = "积分调整值不能小于-100000")
    @Max(value = 100000, message = "积分调整值不能大于100000")
    private Integer amount;
    /**
     * 调整说明。
     */
    private String description;
}
