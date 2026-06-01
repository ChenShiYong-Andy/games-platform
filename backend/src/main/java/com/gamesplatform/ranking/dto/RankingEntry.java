package com.gamesplatform.ranking.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 排行榜条目。
 */
@Data
@Builder
public class RankingEntry {
    /**
     * 用户 ID。
     */
    private Long userId;
    /**
     * 昵称。
     */
    private String nickname;
    /**
     * 头像地址。
     */
    private String avatarUrl;
    /**
     * 得分。
     */
    private Integer score;
    /**
     * 排名。
     */
    private Integer rank;
}
