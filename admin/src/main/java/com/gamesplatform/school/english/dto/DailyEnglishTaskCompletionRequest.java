package com.gamesplatform.school.english.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 每日英语跟读任务完成请求。
 */
@Data
public class DailyEnglishTaskCompletionRequest {

    /**
     * 已正确完成跟读的单词和短句。
     */
    @Schema(description = "已正确完成跟读的单词和短句")
    @NotEmpty(message = "跟读任务不能为空")
    private List<@NotBlank(message = "跟读内容不能为空") String> completedItems;
}
