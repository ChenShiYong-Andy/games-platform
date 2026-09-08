package com.gamesplatform.system.admin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员与普通用户的当前关联实体。
 */
@Data
@TableName("admin_user_relation")
public class AdminUserRelation {

    /** 关联主键。 */
    @Schema(description = "关联主键")
    @TableId(type = IdType.AUTO)
    private Long id;
    /** 管理员主键。 */
    @Schema(description = "管理员主键")
    private Long adminId;
    /** 普通用户主键。 */
    @Schema(description = "普通用户主键")
    private Long userId;
    /** 关联时间。 */
    @Schema(description = "关联时间")
    private LocalDateTime boundAt;
}
