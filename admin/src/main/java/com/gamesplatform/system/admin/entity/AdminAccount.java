package com.gamesplatform.system.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 独立管理员账户实体。
 */
@Data
@TableName("admin_account")
public class AdminAccount {

    /** 管理员主键。 */
    @Schema(description = "管理员主键")
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 管理员登录名。 */
    @Schema(description = "管理员登录名")
    private String username;
    /** 管理员密码哈希。 */
    @Schema(description = "管理员密码哈希")
    private String passwordHash;
    /** 管理员显示名称。 */
    @Schema(description = "管理员显示名称")
    private String displayName;
    /** 账户状态。 */
    @Schema(description = "账户状态")
    private String status;
    /** 令牌版本。 */
    @Schema(description = "令牌版本")
    private Integer tokenVersion;
    /** 最近登录时间。 */
    @Schema(description = "最近登录时间")
    private LocalDateTime lastLoginAt;
    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    /** 更新时间。 */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
