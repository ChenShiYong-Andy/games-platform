package com.gamesplatform.points.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamesplatform.points.dto.PointTransactionResponse;
import com.gamesplatform.points.entity.PointTransaction;
import com.gamesplatform.points.mapper.PointTransactionMapper;
import com.gamesplatform.ranking.service.RankingService;
import com.gamesplatform.user.entity.User;
import com.gamesplatform.user.mapper.UserMapper;
import com.gamesplatform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 积分服务。
 */
@Service
@RequiredArgsConstructor
public class PointsService {

    /**
     * 积分流水数据访问组件。
     */
    private final PointTransactionMapper pointTransactionMapper;
    /**
     * 用户数据访问组件。
     */
    private final UserMapper userMapper;
    /**
     * 用户服务。
     */
    private final UserService userService;
    /**
     * 排行榜服务。
     */
    private final RankingService rankingService;

    /**
     * 日期时间格式化器。
     */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 发放积分。
     *
     * @param userId 用户 ID。
     * @param amount 积分变动值。
     * @param type 类型。
     * @param sourceId 来源业务 ID。
     * @param description 描述。
     * @return 处理结果。
     */
    @Transactional
    public int awardPoints(Long userId, int amount, String type, Long sourceId, String description) {
        return changePoints(userId, amount, type, sourceId, description);
    }

    /**
     * 扣除积分。
     *
     * @param userId 用户 ID。
     * @param requestedAmount 请求扣除的积分值。
     * @param type 类型。
     * @param sourceId 来源业务 ID。
     * @param description 描述。
     * @return 处理结果。
     */
    @Transactional
    public int deductPoints(Long userId, int requestedAmount, String type, Long sourceId, String description) {
        if (requestedAmount <= 0) {
            return userService.getUserById(userId).getTotalPoints();
        }
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getId, userId)
                        .last("FOR UPDATE"));
        int deduction = Math.min(user.getTotalPoints(), requestedAmount);
        if (deduction == 0) {
            return 0;
        }
        return changePoints(user, -deduction, type, sourceId, description);
    }

    private int changePoints(Long userId, int amount, String type, Long sourceId, String description) {
        User user = userMapper.selectById(userId);
        return changePoints(user, amount, type, sourceId, description);
    }

    private int changePoints(User user, int amount, String type, Long sourceId, String description) {
        PointTransaction tx = new PointTransaction();
        tx.setUserId(user.getId());
        tx.setAmount(amount);
        tx.setType(type);
        tx.setSourceId(sourceId);
        tx.setDescription(description);
        tx.setCreatedAt(LocalDateTime.now());
        pointTransactionMapper.insert(tx);

        int newTotal = Math.max(0, user.getTotalPoints() + amount);
        user.setTotalPoints(newTotal);
        user.setLevel(userService.calculateLevel(newTotal));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        rankingService.updateTotalPoints(user.getId(), newTotal);
        rankingService.updateWeeklyPoints(user.getId(), amount);

        return newTotal;
    }

    /**
     * 查询积分流水。
     *
     * @param userId 用户 ID。
     * @param limit 查询数量上限。
     * @return 处理结果。
     */
    public List<PointTransactionResponse> getTransactions(Long userId, int limit) {
        List<PointTransaction> list = pointTransactionMapper.selectList(
                new LambdaQueryWrapper<PointTransaction>()
                        .eq(PointTransaction::getUserId, userId)
                        .orderByDesc(PointTransaction::getCreatedAt)
                        .last("LIMIT " + limit));
        return list.stream().map(tx -> PointTransactionResponse.builder()
                .id(tx.getId())
                .amount(tx.getAmount())
                .type(tx.getType())
                .description(tx.getDescription())
                .createdAt(tx.getCreatedAt().format(FORMATTER))
                .build()).toList();
    }
}
