package com.gamesplatform.system.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 独立管理后台状态。
 */
@Data
@AllArgsConstructor
public class AdminPortalStatusResponse {

    private boolean passwordSet;
}
