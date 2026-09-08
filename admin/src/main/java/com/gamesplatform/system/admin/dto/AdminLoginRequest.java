package com.gamesplatform.system.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 管理员登录请求。 */
@Data
public class AdminLoginRequest {
    /** 管理员登录名。 */
    @Schema(description = "管理员登录名")
    @NotBlank(message = "管理员账号不能为空")
    private String username;
    /** 管理员密码。 */
    @Schema(description = "管理员密码")
    @NotBlank(message = "管理员密码不能为空")
    private String password;
}
