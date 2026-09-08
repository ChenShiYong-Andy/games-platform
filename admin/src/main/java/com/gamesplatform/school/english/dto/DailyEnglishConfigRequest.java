package com.gamesplatform.school.english.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 每日英语管理配置更新请求。
 */
@Data
public class DailyEnglishConfigRequest {

    /**
     * 口语练习适用年级，取值范围为小学一年级至六年级。
     */
    @Schema(description = "口语练习适用年级，取值范围为 1 至 6", example = "3")
    @NotNull(message = "请选择学习年级")
    @Min(value = 1, message = "年级不能小于一年级")
    @Max(value = 6, message = "年级不能大于六年级")
    private Integer gradeLevel;

    /**
     * 生成口语练习时追加给大模型的 Skill Markdown 内容。
     */
    @Schema(description = "生成口语练习时追加给大模型的 Skill Markdown 内容")
    @Size(max = 20000, message = "Skill 内容不能超过 20000 个字符")
    private String skillMarkdown;

}
