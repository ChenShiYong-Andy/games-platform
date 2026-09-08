package com.gamesplatform.system.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** 管理员绑定普通用户请求。 */
@Data
public class AdminBindUserRequest {
    /** 普通用户登录名。 */
    @Schema(description = "普通用户登录名")
    @NotBlank(message = "用户账号不能为空")
    private String username;
    /** 普通用户生成的一次性绑定码。 */
    @Schema(description = "普通用户生成的一次性绑定码")
    @NotBlank(message = "绑定码不能为空")
    private String bindingCode;
}
