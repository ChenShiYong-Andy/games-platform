package com.gamesplatform.ranking.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RankingEntry {
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private Integer score;
    private Integer rank;
}
