package com.gamesplatform.school.english.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 每日英语练习缓存。
 */
@Data
@TableName("daily_english_practice")
public class DailyEnglishPractice {

    /**
     * 每日英语练习缓存主键。
     */
    @Schema(description = "每日英语练习缓存主键")
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 练习内容对应的自然日。
     */
    @Schema(description = "练习内容对应的自然日")
    private LocalDate practiceDate;

    /**
     * 生成练习时使用的年级配置。
     */
    @Schema(description = "生成练习时使用的年级配置，取值范围为 1 至 6")
    private Integer gradeLevel;

    /**
     * 序列化后的完整练习响应 JSON。
     */
    @Schema(description = "序列化后的完整练习响应 JSON")
    private String contentJson;

    /**
     * 缓存记录创建时间。
     */
    @Schema(description = "缓存记录创建时间")
    private LocalDateTime createdAt;
}
