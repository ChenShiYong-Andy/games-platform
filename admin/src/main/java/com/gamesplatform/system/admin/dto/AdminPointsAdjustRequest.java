package com.gamesplatform.system.admin.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理后台积分调整请求。
 */
@Data
public class AdminPointsAdjustRequest {

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
