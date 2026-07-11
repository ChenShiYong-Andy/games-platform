package com.gamesplatform.admin.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 管理配置状态响应。
 */
@Data
@Builder
public class AdminConfigStatusResponse {

    /**
     * 是否已设置管理密码。
     */
    private Boolean adminPasswordSet;
}
