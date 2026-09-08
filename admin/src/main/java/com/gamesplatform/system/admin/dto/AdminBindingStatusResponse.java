package com.gamesplatform.system.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 普通用户的管理员关联状态响应。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminBindingStatusResponse {
    /** 是否已经关联管理员。 */
    @Schema(description = "是否已经关联管理员")
    private boolean bound;
    /** 管理员登录名。 */
    @Schema(description = "管理员登录名")
    private String adminUsername;
    /** 管理员显示名称。 */
    @Schema(description = "管理员显示名称")
    private String adminDisplayName;
}
