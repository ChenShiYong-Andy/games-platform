package com.gamesplatform.school.english.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 英语口语练习项。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyEnglishItemResponse {

    /**
     * 练习项类型，单词为 WORD，短句为 SENTENCE。
     */
    @Schema(description = "练习项类型：WORD 或 SENTENCE", example = "WORD")
    private String type;

    /**
     * 需要播放和跟读的英文内容。
     */
    @Schema(description = "需要播放和跟读的英文内容", example = "apple")
    private String text;

    /**
     * 单词的常见音标；短句可以为空。
     */
    @Schema(description = "单词的常见音标；短句可以为空", example = "/ˈæp.əl/")
    private String phonetic;

    /**
     * 英文内容对应的简洁中文翻译。
     */
    @Schema(description = "英文内容对应的简洁中文翻译", example = "苹果")
    private String translation;

    /**
     * 面向学习者的简短发音提示。
     */
    @Schema(description = "面向学习者的简短发音提示")
    private String tip;
}
