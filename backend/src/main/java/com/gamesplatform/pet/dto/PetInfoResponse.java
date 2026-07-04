package com.gamesplatform.pet.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 宠物信息响应。
 */
@Data
@Builder
public class PetInfoResponse {

    /**
     * 宠物 ID。
     */
    private Long petId;
    /**
     * 宠物类型。
     */
    private String petType;
    /**
     * 宠物类型名称。
     */
    private String petTypeName;
    /**
     * 宠物颜色编码。
     */
    private String petColorCode;
    /**
     * 宠物颜色名称。
     */
    private String petColorName;
    /**
     * 宠物名称。
     */
    private String petName;
    /**
     * 等级。
     */
    private Integer level;
    /**
     * 当前成长阶段。
     */
    private Integer stageNo;
    /**
     * 当前成长阶段名称。
     */
    private String stageName;
    /**
     * 当前宠物资源标识。
     */
    private String petAssetKey;
    /**
     * 下一阶段信息。
     */
    private PetNextStageResponse nextStage;
    /**
     * 成长值。
     */
    private Integer exp;
    /**
     * 饥饿值。
     */
    private Integer hunger;
    /**
     * 清洁值。
     */
    private Integer clean;
    /**
     * 快乐值。
     */
    private Integer happiness;
    /**
     * 体力值。
     */
    private Integer energy;
    /**
     * 爱心值。
     */
    private Integer loveValue;
    /**
     * 当前头饰。
     */
    private String currentHatCode;
    /**
     * 当前小床。
     */
    private String currentBedCode;
    /**
     * 当前房间主题。
     */
    private String currentRoomTheme;
}
