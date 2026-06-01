package com.gamesplatform.zoo.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.zoo.dto.ZooCareRequest;
import com.gamesplatform.zoo.dto.ZooCareResponse;
import com.gamesplatform.zoo.service.ZooService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 动物园游戏接口。
 */
@RestController
@RequestMapping("/api/zoo")
@RequiredArgsConstructor
public class ZooController {

    /**
     * 动物园游戏服务。
     */
    private final ZooService zooService;

    /**
     * 开始动物园游戏。
     *
     * @param authentication 当前认证信息。
     * @return 处理结果。
     */
    @PostMapping("/games")
    public ApiResponse<ZooCareResponse> startGame(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(zooService.startGame(userId));
    }

    /**
     * 记录动物园照料操作。
     *
     * @param authentication 当前认证信息。
     * @param gameId 游戏 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/games/{gameId}/care")
    public ApiResponse<ZooCareResponse> recordCare(
            Authentication authentication,
            @PathVariable Long gameId,
            @RequestBody ZooCareRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(zooService.recordCare(userId, gameId, request.getTool()));
    }

    /**
     * 结算动物园游戏。
     *
     * @param authentication 当前认证信息。
     * @param gameId 游戏 ID。
     * @return 处理结果。
     */
    @PostMapping("/games/{gameId}/settle")
    public ApiResponse<ZooCareResponse> settleGame(
            Authentication authentication,
            @PathVariable Long gameId) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(zooService.settleGame(userId, gameId));
    }
}
