package com.gamesplatform.school.english.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 大模型生成的结构化英语练习内容。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyEnglishGeneratedContent {

    /**
     * 本次口语练习的中文主题名称。
     */
    @Schema(description = "本次口语练习的中文主题名称", example = "快乐校园")
    private String title;

    /**
     * 大模型生成的单词和短句练习项。
     */
    @Schema(description = "大模型生成的单词和短句练习项")
    private List<DailyEnglishItemResponse> items;
}
