package com.gamesplatform.pet.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 宠物类型选项响应。
 */
@Data
@Builder
public class PetTypeOptionResponse {

    /**
     * 宠物类型编码。
     */
    private String petType;
    /**
     * 宠物类型名称。
     */
    private String petTypeName;
    /**
     * 说明。
     */
    private String description;
    /**
     * 默认颜色编码。
     */
    private String defaultColorCode;
    /**
     * 颜色选项。
     */
    private List<PetColorOptionResponse> colors;
}
