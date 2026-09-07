package com.gamesplatform.school.pet.service;

import com.gamesplatform.school.pet.dto.PetBenefitListResponse;
import com.gamesplatform.school.pet.dto.PetExchangeResponse;
import com.gamesplatform.school.pet.dto.PetHomeResponse;
import com.gamesplatform.school.pet.dto.PetInfoResponse;
import com.gamesplatform.school.pet.dto.PetInitOptionsResponse;
import com.gamesplatform.school.pet.dto.PetInitSelectRequest;
import com.gamesplatform.school.pet.dto.PetProfileResponse;
import com.gamesplatform.school.pet.dto.PetUseBenefitResponse;
import com.gamesplatform.school.pet.dto.PetUserBenefitListResponse;

/**
 * 宠物养成业务服务。
 */
public interface PetService {

    /**
     * 查询用户的宠物档案。
     *
     * @param userId 当前用户 ID。
     * @return 宠物档案。
     */
    PetProfileResponse getProfile(Long userId);

    /**
     * 查询宠物初始化选项。
     *
     * @return 宠物初始化选项。
     */
    PetInitOptionsResponse getInitOptions();

    /**
     * 为用户选择初始宠物。
     *
     * @param userId 当前用户 ID。
     * @param request 初始宠物选择请求。
     * @return 宠物信息。
     */
    PetInfoResponse selectPet(Long userId, PetInitSelectRequest request);

    /**
     * 查询宠物首页聚合信息。
     *
     * @param userId 当前用户 ID。
     * @return 宠物首页信息。
     */
    PetHomeResponse getHome(Long userId);

    /**
     * 增加当前宠物的成长值。
     *
     * @param userId 当前用户 ID。
     * @return 更新后的宠物信息。
     */
    PetInfoResponse growPet(Long userId);

    /**
     * 查询可兑换权益列表。
     *
     * @param userId 当前用户 ID。
     * @param benefitType 权益类型。
     * @return 权益列表。
     */
    PetBenefitListResponse getBenefitList(Long userId, String benefitType);

    /**
     * 兑换指定权益。
     *
     * @param userId 当前用户 ID。
     * @param benefitId 权益 ID。
     * @param quantity 兑换数量。
     * @return 兑换结果。
     */
    PetExchangeResponse exchangeBenefit(Long userId, Long benefitId, Integer quantity);

    /**
     * 查询用户背包中的权益。
     *
     * @param userId 当前用户 ID。
     * @param benefitType 权益类型。
     * @param status 使用状态。
     * @return 用户权益列表。
     */
    PetUserBenefitListResponse getMyBenefits(Long userId, String benefitType, Integer status);

    /**
     * 使用背包中的指定权益。
     *
     * @param userId 当前用户 ID。
     * @param userBenefitId 用户权益 ID。
     * @param useAll 是否按需一键使用。
     * @return 使用结果。
     */
    PetUseBenefitResponse useBenefit(Long userId, Long userBenefitId, boolean useAll);
}
