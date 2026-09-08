package com.gamesplatform.school.english.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户级每日英语生成配置实体。
 */
@Data
@TableName("daily_english_user_config")
public class DailyEnglishUserConfig {

    /** 用户主键。 */
    @Schema(description = "用户主键")
    @TableId
    private Long userId;
    /** 小学年级。 */
    @Schema(description = "小学年级，取值 1 至 6")
    private Integer gradeLevel;
    /** 追加给大模型的 Skill Markdown。 */
    @Schema(description = "追加给大模型的 Skill Markdown")
    private String skillMarkdown;
    /** 最后更新配置的管理员主键。 */
    @Schema(description = "最后更新配置的管理员主键")
    private Long updatedByAdminId;
    /** 乐观锁版本。 */
    @Schema(description = "乐观锁版本")
    private Integer version;
    /** 创建时间。 */
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
    /** 更新时间。 */
    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;
}
