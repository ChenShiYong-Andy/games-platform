package com.gamesplatform.system.points.service;

import com.gamesplatform.system.points.dto.PointTransactionResponse;

import java.util.List;

/**
 * 积分业务服务。
 */
public interface PointsService {

    /**
     * 为用户发放积分。
     *
     * @param userId 用户 ID。
     * @param amount 发放积分。
     * @param type 积分类型。
     * @param sourceId 来源业务 ID。
     * @param description 积分描述。
     * @return 变动后的总积分。
     */
    int awardPoints(Long userId, int amount, String type, Long sourceId, String description);

    /**
     * 在用户现有积分范围内扣除积分。
     *
     * @param userId 用户 ID。
     * @param requestedAmount 请求扣除的积分。
     * @param type 积分类型。
     * @param sourceId 来源业务 ID。
     * @param description 积分描述。
     * @return 变动后的总积分。
     */
    int deductPoints(Long userId, int requestedAmount, String type, Long sourceId, String description);

    /**
     * 严格扣除积分，积分不足时拒绝操作。
     *
     * @param userId 用户 ID。
     * @param amount 扣除积分。
     * @param type 积分类型。
     * @param sourceId 来源业务 ID。
     * @param description 积分描述。
     * @return 变动后的总积分。
     */
    int consumePoints(Long userId, int amount, String type, Long sourceId, String description);

    /**
     * 查询用户最近的积分流水。
     *
     * @param userId 用户 ID。
     * @param limit 返回数量上限。
     * @return 积分流水列表。
     */
    List<PointTransactionResponse> getTransactions(Long userId, int limit);
}
