package com.gamesplatform.pet.controller;

import com.gamesplatform.common.ApiResponse;
import com.gamesplatform.pet.dto.*;
import com.gamesplatform.pet.service.PetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 宠物养成接口。
 */
@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
public class PetController {

    /**
     * 宠物养成服务。
     */
    private final PetService petService;

    /**
     * 查询我的宠物资料。
     *
     * @param authentication 当前认证信息。
     * @return 处理结果。
     */
    @GetMapping("/profile")
    public ApiResponse<PetProfileResponse> getProfile(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(petService.getProfile(userId));
    }

    /**
     * 查询首次领养选项。
     *
     * @return 处理结果。
     */
    @GetMapping("/init/options")
    public ApiResponse<PetInitOptionsResponse> getInitOptions() {
        return ApiResponse.success(petService.getInitOptions());
    }

    /**
     * 首次领养宠物。
     *
     * @param authentication 当前认证信息。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/init/select")
    public ApiResponse<PetInfoResponse> selectPet(
            Authentication authentication,
            @Valid @RequestBody PetInitSelectRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("领养成功", petService.selectPet(userId, request));
    }

    /**
     * 查询宠物首页。
     *
     * @param authentication 当前认证信息。
     * @return 处理结果。
     */
    @GetMapping("/home")
    public ApiResponse<PetHomeResponse> getHome(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(petService.getHome(userId));
    }

    /**
     * 查询宠物权益列表。
     *
     * @param authentication 当前认证信息。
     * @param benefitType 权益类型。
     * @return 处理结果。
     */
    @GetMapping("/benefit/list")
    public ApiResponse<PetBenefitListResponse> getBenefitList(
            Authentication authentication,
            @RequestParam(required = false) String benefitType) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(petService.getBenefitList(userId, benefitType));
    }

    /**
     * 兑换宠物权益。
     *
     * @param authentication 当前认证信息。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/benefit/exchange")
    public ApiResponse<PetExchangeResponse> exchangeBenefit(
            Authentication authentication,
            @Valid @RequestBody PetExchangeRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("兑换成功", petService.exchangeBenefit(userId, request.getBenefitId()));
    }

    /**
     * 查询我的宠物权益。
     *
     * @param authentication 当前认证信息。
     * @param benefitType 权益类型。
     * @param status 状态。
     * @return 处理结果。
     */
    @GetMapping("/benefit/my")
    public ApiResponse<PetUserBenefitListResponse> getMyBenefits(
            Authentication authentication,
            @RequestParam(required = false) String benefitType,
            @RequestParam(required = false) Integer status) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success(petService.getMyBenefits(userId, benefitType, status));
    }

    /**
     * 使用宠物权益。
     *
     * @param authentication 当前认证信息。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @PostMapping("/benefit/use")
    public ApiResponse<PetUseBenefitResponse> useBenefit(
            Authentication authentication,
            @Valid @RequestBody PetUseBenefitRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        return ApiResponse.success("使用成功", petService.useBenefit(userId, request.getUserBenefitId()));
    }
}
