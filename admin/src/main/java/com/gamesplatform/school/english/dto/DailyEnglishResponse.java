package com.gamesplatform.school.english.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日英语口语练习响应。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyEnglishResponse {

    /**
     * 练习内容对应的自然日。
     */
    @Schema(description = "练习内容对应的自然日", example = "2026-09-07")
    private LocalDate date;

    /**
     * 口语练习适用年级，取值范围为小学一年级至六年级。
     */
    @Schema(description = "口语练习适用年级，取值范围为 1 至 6", example = "3")
    private Integer gradeLevel;

    /**
     * 适用年级的中文显示名称。
     */
    @Schema(description = "适用年级的中文显示名称", example = "小学三年级")
    private String gradeLabel;

    /**
     * 当天口语练习的中文主题名称。
     */
    @Schema(description = "当天口语练习的中文主题名称", example = "快乐校园")
    private String title;

    /**
     * 当天需要播放和跟读的单词及短句列表。
     */
    @Schema(description = "当天需要播放和跟读的单词及短句列表")
    private List<DailyEnglishItemResponse> items;
}
