package com.gamesplatform.system.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理员注册请求。 */
@Data
public class AdminRegisterRequest {
    /** 管理员登录名。 */
    @Schema(description = "管理员登录名")
    @NotBlank(message = "管理员账号不能为空")
    @Size(min = 3, max = 64, message = "管理员账号长度应为 3 至 64 位")
    private String username;
    /** 管理员密码。 */
    @Schema(description = "管理员密码")
    @NotBlank(message = "管理员密码不能为空")
    @Size(min = 6, max = 72, message = "管理员密码长度应为 6 至 72 位")
    private String password;
    /** 管理员显示名称。 */
    @Schema(description = "管理员显示名称")
    @NotBlank(message = "显示名称不能为空")
    @Size(max = 64, message = "显示名称不能超过 64 位")
    private String displayName;
}
