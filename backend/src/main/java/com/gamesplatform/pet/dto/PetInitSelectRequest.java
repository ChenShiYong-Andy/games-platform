package com.gamesplatform.pet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 首次领养宠物请求。
 */
@Data
public class PetInitSelectRequest {

    /**
     * 宠物类型编码。
     */
    @NotBlank(message = "宠物类型不能为空")
    private String petType;
    /**
     * 颜色编码。未传时使用宠物类型默认颜色。
     */
    private String colorCode;
    /**
     * 宠物名称。
     */
    private String petName;
}
