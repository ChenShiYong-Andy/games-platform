package com.gamesplatform.system.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 普通用户生成的管理员绑定码响应。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminBindingCodeResponse {
    /** 一次性绑定码。 */
    @Schema(description = "一次性绑定码")
    private String code;
    /** 绑定码过期时间。 */
    @Schema(description = "绑定码过期时间")
    private LocalDateTime expiresAt;
}
