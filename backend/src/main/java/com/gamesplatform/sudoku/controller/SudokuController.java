package com.gamesplatform.sudoku.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.sudoku.dto.*;
import com.gamesplatform.sudoku.service.SudokuService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sudoku")
@RequiredArgsConstructor
public class SudokuController {

    private final SudokuService sudokuService;

    @PostMapping("/games")
    public ApiResponse<GameResponse> createGame(
            Authentication authentication,
            @RequestBody CreateGameRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(sudokuService.createGame(userId, request.getDifficulty()));
    }

    @GetMapping("/games/{gameId}")
    public ApiResponse<GameResponse> getGame(
            Authentication authentication,
            @PathVariable Long gameId) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(sudokuService.getGame(userId, gameId));
    }

    @GetMapping("/games")
    public ApiResponse<List<GameResponse>> getGameHistory(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(sudokuService.getGameHistory(userId, limit));
    }

    @PostMapping("/games/{gameId}/validate")
    public ApiResponse<ValidateMoveResponse> validateMove(
            Authentication authentication,
            @PathVariable Long gameId,
            @RequestBody ValidateMoveRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(sudokuService.validateMove(userId, gameId, request));
    }

    @PostMapping("/games/{gameId}/hint")
    public ApiResponse<HintResponse> getHint(
            Authentication authentication,
            @PathVariable Long gameId) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(sudokuService.getHint(userId, gameId));
    }

    @PostMapping("/games/{gameId}/submit")
    public ApiResponse<SubmitGameResponse> submitGame(
            Authentication authentication,
            @PathVariable Long gameId,
            @RequestBody SubmitGameRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(sudokuService.submitGame(userId, gameId, request));
    }
}
