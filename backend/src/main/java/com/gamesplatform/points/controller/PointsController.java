package com.gamesplatform.points.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.points.dto.PointTransactionResponse;
import com.gamesplatform.points.service.PointsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/points")
@RequiredArgsConstructor
public class PointsController {

    private final PointsService pointsService;

    @GetMapping("/transactions")
    public ApiResponse<List<PointTransactionResponse>> getTransactions(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(pointsService.getTransactions(userId, limit));
    }
}
