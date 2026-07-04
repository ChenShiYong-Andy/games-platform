package com.gamesplatform.pet.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 宠物颜色选项响应。
 */
@Data
@Builder
public class PetColorOptionResponse {

    /**
     * 颜色编码。
     */
    private String colorCode;
    /**
     * 颜色名称。
     */
    private String colorName;
    /**
     * 颜色值。
     */
    private String colorHex;
    /**
     * 基础资源标识。
     */
    private String assetKey;
    /**
     * 成长阶段预览。
     */
    private List<PetGrowthStageResponse> stagePreviewList;
}
