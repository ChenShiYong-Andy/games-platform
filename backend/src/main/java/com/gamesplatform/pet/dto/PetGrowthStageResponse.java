package com.gamesplatform.pet.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 宠物成长阶段响应。
 */
@Data
@Builder
public class PetGrowthStageResponse {

    /**
     * 阶段编号。
     */
    private Integer stageNo;
    /**
     * 阶段名称。
     */
    private String stageName;
    /**
     * 最小等级。
     */
    private Integer minLevel;
    /**
     * 最大等级。
     */
    private Integer maxLevel;
    /**
     * 资源标识。
     */
    private String assetKey;
    /**
     * 预览资源标识。
     */
    private String previewAssetKey;
    /**
     * 阶段说明。
     */
    private String description;
}
