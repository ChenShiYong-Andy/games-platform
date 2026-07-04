package com.gamesplatform.pet.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 宠物权益使用响应。
 */
@Data
@Builder
public class PetUseBenefitResponse {

    /**
     * 权益编码。
     */
    private String benefitCode;
    /**
     * 权益名称。
     */
    private String benefitName;
    /**
     * 剩余数量。
     */
    private Integer remainingQuantity;
    /**
     * 宠物信息。
     */
    private PetInfoResponse petInfo;
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
