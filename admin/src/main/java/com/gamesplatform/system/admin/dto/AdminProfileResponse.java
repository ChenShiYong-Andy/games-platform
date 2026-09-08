package com.gamesplatform.system.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 管理员资料响应。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminProfileResponse {
    /** 管理员主键。 */
    @Schema(description = "管理员主键")
    private Long id;
    /** 管理员登录名。 */
    @Schema(description = "管理员登录名")
    private String username;
    /** 管理员显示名称。 */
    @Schema(description = "管理员显示名称")
    private String displayName;
}
