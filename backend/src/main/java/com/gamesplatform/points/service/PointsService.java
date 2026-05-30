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

@Service
@RequiredArgsConstructor
public class PointsService {

    private final PointTransactionMapper pointTransactionMapper;
    private final UserMapper userMapper;
    private final UserService userService;
    private final RankingService rankingService;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Transactional
    public int awardPoints(Long userId, int amount, String type, Long sourceId, String description) {
        PointTransaction tx = new PointTransaction();
        tx.setUserId(userId);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setSourceId(sourceId);
        tx.setDescription(description);
        tx.setCreatedAt(LocalDateTime.now());
        pointTransactionMapper.insert(tx);

        User user = userMapper.selectById(userId);
        int newTotal = user.getTotalPoints() + amount;
        user.setTotalPoints(newTotal);
        user.setLevel(userService.calculateLevel(newTotal));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);

        rankingService.updateTotalPoints(userId, newTotal);
        rankingService.updateWeeklyPoints(userId, amount);

        return newTotal;
    }

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
