package com.gamesplatform.points.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PointTransactionResponse {
    private Long id;
    private Integer amount;
    private String type;
    private String description;
    private String createdAt;
}
