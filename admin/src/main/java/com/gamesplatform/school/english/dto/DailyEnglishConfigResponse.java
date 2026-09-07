package com.gamesplatform.school.english.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 每日英语管理配置响应。
 */
@Data
@AllArgsConstructor
public class DailyEnglishConfigResponse {

    /**
     * 口语练习适用年级，取值范围为小学一年级至六年级。
     */
    @Schema(description = "口语练习适用年级，取值范围为 1 至 6", example = "3")
    private Integer gradeLevel;

    /**
     * 生成口语练习时追加给大模型的 Skill Markdown 内容。
     */
    @Schema(description = "生成口语练习时追加给大模型的 Skill Markdown 内容")
    private String skillMarkdown;
}
