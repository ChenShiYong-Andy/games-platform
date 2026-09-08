package com.gamesplatform.system.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员操作审计日志实体。
 */
@Data
@TableName("admin_operation_log")
public class AdminOperationLog {

    /** 日志主键。 */
    @Schema(description = "日志主键")
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 操作管理员主键。 */
    @Schema(description = "操作管理员主键")
    private Long adminId;
    /** 被管理用户主键。 */
    @Schema(description = "被管理用户主键")
    private Long userId;
    /** 业务模块。 */
    @Schema(description = "业务模块")
    private String module;
    /** 操作名称。 */
    @Schema(description = "操作名称")
    private String action;
    /** 操作前数据。 */
    @Schema(description = "操作前数据")
    private String beforeData;
    /** 操作后数据。 */
    @Schema(description = "操作后数据")
    private String afterData;
    /** 请求追踪标识。 */
    @Schema(description = "请求追踪标识")
    private String requestId;
    /** 客户端地址。 */
    @Schema(description = "客户端地址")
    private String ipAddress;
    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
