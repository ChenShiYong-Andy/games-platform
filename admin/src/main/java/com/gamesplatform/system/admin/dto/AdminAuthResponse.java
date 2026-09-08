package com.gamesplatform.system.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 管理员认证响应。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminAuthResponse {
    /** 管理员访问令牌。 */
    @Schema(description = "管理员访问令牌")
    private String token;
    /** 管理员资料。 */
    @Schema(description = "管理员资料")
    private AdminProfileResponse admin;
}
