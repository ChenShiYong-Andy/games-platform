package com.gamesplatform.points.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.points.dto.PointTransactionResponse;
import com.gamesplatform.points.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分接口。
 */
@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {

    /**
     * 积分服务。
     */
    private final PointsService pointsService;

    /**
     * 查询积分流水。
     *
     * @param authentication 当前认证信息。
     * @param limit 查询数量上限。
     * @return 处理结果。
     */
    @GetMapping("/transactions")
    public ApiResponse<List<PointTransactionResponse>> getTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(pointsService.getTransactions(userId, limit));
    }
}
