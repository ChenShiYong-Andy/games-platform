package com.gamesplatform.sudoku.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.sudoku.dto.*;
import com.gamesplatform.sudoku.service.SudokuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数独接口。
 */
@RestController
@RequestMapping("/api/sudoku")
@RequiredArgsConstructor
public class SudokuController {

    /**
     * 数独服务。
     */
    private final SudokuService sudokuService;

    /**
     * 创建游戏。
     *
     * @param authentication 当前认证信息。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/games")
    public ApiResponse<GameResponse> createGame(
            Authentication authentication,
            @RequestBody CreateGameRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(sudokuService.createGame(userId, request.getDifficulty()));
    }

    /**
     * 查询游戏。
     *
     * @param authentication 当前认证信息。
     * @param gameId 游戏 ID。
     * @return 处理结果。
     */
    @GetMapping("/games/{gameId}")
    public ApiResponse<GameResponse> getGame(
            Authentication authentication,
            @PathVariable Long gameId) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(sudokuService.getGame(userId, gameId));
    }

    /**
     * 查询游戏历史。
     *
     * @param authentication 当前认证信息。
     * @param limit 查询数量上限。
     * @return 处理结果。
     */
    @GetMapping("/games")
    public ApiResponse<List<GameResponse>> getGameHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(sudokuService.getGameHistory(userId, limit));
    }

    /**
     * 校验落子。
     *
     * @param authentication 当前认证信息。
     * @param gameId 游戏 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/games/{gameId}/validate")
    public ApiResponse<ValidateMoveResponse> validateMove(
            Authentication authentication,
            @PathVariable Long gameId,
            @RequestBody ValidateMoveRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(sudokuService.validateMove(userId, gameId, request));
    }

    /**
     * 获取提示。
     *
     * @param authentication 当前认证信息。
     * @param gameId 游戏 ID。
     * @return 处理结果。
     */
    @PostMapping("/games/{gameId}/hint")
    public ApiResponse<HintResponse> getHint(
            Authentication authentication,
            @PathVariable Long gameId) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(sudokuService.getHint(userId, gameId));
    }

    /**
     * 提交游戏。
     *
     * @param authentication 当前认证信息。
     * @param gameId 游戏 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/games/{gameId}/submit")
    public ApiResponse<SubmitGameResponse> submitGame(
            Authentication authentication,
            @PathVariable Long gameId,
            @RequestBody SubmitGameRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(sudokuService.submitGame(userId, gameId, request));
    }
}
