package com.gamesplatform.system.admin.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 独立管理后台安全配置。
 */
@Data
@TableName("admin_portal_config")
public class AdminPortalConfig {

    @TableId
    private Long id;
    private String passwordHash;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
