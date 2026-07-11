package com.gamesplatform.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 管理员数独配置请求。
 */
@Data
public class AdminGameConfigRequest {

    /**
     * 管理密码。
     */
    @NotBlank(message = "管理密码不能为空")
    private String adminPassword;
    /**
     * 每日数独次数上限。
     */
    @NotNull(message = "每日数独次数不能为空")
    @Min(value = 1, message = "每日数独次数上限不能小于1")
    @Max(value = 50, message = "每日数独次数上限不能大于50")
    private Integer sudokuDailyLimit;
}
