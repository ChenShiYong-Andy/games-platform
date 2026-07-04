package com.gamesplatform.pet.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 宠物下一成长阶段响应。
 */
@Data
@Builder
public class PetNextStageResponse {

    /**
     * 阶段编号。
     */
    private Integer stageNo;
    /**
     * 阶段名称。
     */
    private String stageName;
    /**
     * 所需等级。
     */
    private Integer needLevel;
    /**
     * 剩余等级。
     */
    private Integer remainLevel;
}
