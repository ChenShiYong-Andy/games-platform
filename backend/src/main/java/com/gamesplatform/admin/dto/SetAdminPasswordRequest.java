package com.gamesplatform.admin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 设置管理密码请求。
 */
@Data
public class SetAdminPasswordRequest {

    /**
     * 管理密码。
     */
    @NotBlank(message = "管理密码不能为空")
    private String password;
}
