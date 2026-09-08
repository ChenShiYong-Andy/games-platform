package com.gamesplatform.system.admin.dto;

import com.gamesplatform.system.user.dto.UserProfileResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 管理员工作台中的已关联用户响应。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagedUserResponse {
    /** 普通用户资料。 */
    @Schema(description = "普通用户资料")
    private UserProfileResponse user;
    /** 关联时间。 */
    @Schema(description = "关联时间")
    private LocalDateTime boundAt;
}
