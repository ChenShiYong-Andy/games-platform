package com.gamesplatform.system.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 独立管理后台密码请求。
 */
@Data
public class AdminPortalPasswordRequest {

    @NotBlank(message = "管理后台密码不能为空")
    @Size(min = 6, max = 72, message = "管理后台密码长度必须在 6 到 72 位之间")
    private String password;
}
