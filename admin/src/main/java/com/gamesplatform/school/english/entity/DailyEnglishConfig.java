package com.gamesplatform.school.english.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 每日英语生成配置。
 */
@Data
@TableName("daily_english_config")
public class DailyEnglishConfig {

    /**
     * 配置主键；每日英语使用固定的单例配置记录。
     */
    @Schema(description = "配置主键")
    @TableId
    private Long id;

    /**
     * 口语练习适用年级，取值范围为小学一年级至六年级。
     */
    @Schema(description = "口语练习适用年级，取值范围为 1 至 6")
    private Integer gradeLevel;

    /**
     * 生成口语练习时追加给大模型的 Skill Markdown 内容。
     */
    @Schema(description = "生成口语练习时追加给大模型的 Skill Markdown 内容")
    private String skillMarkdown;

    /**
     * 配置最后更新时间。
     */
    @Schema(description = "配置最后更新时间")
    private LocalDateTime updatedAt;
}
