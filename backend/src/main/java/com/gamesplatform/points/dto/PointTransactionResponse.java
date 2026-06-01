package com.gamesplatform.points.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 积分流水响应。
 */
@Data
@Builder
public class PointTransactionResponse {
    /**
     * 主键 ID。
     */
    private Long id;
    /**
     * 积分变动值。
     */
    private Integer amount;
    /**
     * 类型。
     */
    private String type;
    /**
     * 描述。
     */
    private String description;
    /**
     * 创建时间。
     */
    private String createdAt;
}
