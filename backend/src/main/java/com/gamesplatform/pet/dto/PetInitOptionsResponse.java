package com.gamesplatform.pet.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 宠物初始化选项响应。
 */
@Data
@Builder
public class PetInitOptionsResponse {

    /**
     * 宠物类型选项。
     */
    private List<PetTypeOptionResponse> petTypes;
}
