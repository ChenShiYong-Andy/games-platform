package com.gamesplatform.pet.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.pet.dto.*;
import com.gamesplatform.pet.entity.*;
import com.gamesplatform.pet.mapper.*;
import com.gamesplatform.points.service.PointsService;
import com.gamesplatform.user.entity.User;
import com.gamesplatform.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 宠物养成服务。
 */
@Service
@RequiredArgsConstructor
public class PetService {

    private static final String TYPE_CONSUMABLE = "CONSUMABLE";
    private static final String TYPE_PERMANENT = "PERMANENT";
    private static final int STATUS_AVAILABLE = 1;
    private static final int STATUS_USED = 2;
    private static final int ORDER_PROCESSING = 0;
    private static final int ORDER_SUCCESS = 1;
    private static final int ORDER_FAILED = 2;
    private static final int MAX_LEVEL = 70;
    private static final int INITIAL_PET_STATUS_VALUE = 50;
    private static final String POINT_TYPE_EXCHANGE = "PET_BENEFIT_EXCHANGE";
    private static final DateTimeFormatter ORDER_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    /**
     * 权益配置数据访问组件。
     */
    private final PetBenefitConfigMapper benefitConfigMapper;
    /**
     * 用户权益数据访问组件。
     */
    private final PetUserBenefitMapper userBenefitMapper;
    /**
     * 兑换订单数据访问组件。
     */
    private final PetBenefitExchangeOrderMapper exchangeOrderMapper;
    /**
     * 使用日志数据访问组件。
     */
    private final PetBenefitUseLogMapper useLogMapper;
    /**
     * 用户宠物数据访问组件。
     */
    private final PetUserMapper petUserMapper;
    /**
     * 宠物类型配置数据访问组件。
     */
    private final PetTypeConfigMapper petTypeConfigMapper;
    /**
     * 宠物颜色配置数据访问组件。
     */
    private final PetColorConfigMapper petColorConfigMapper;
    /**
     * 成长阶段配置数据访问组件。
     */
    private final PetGrowthStageConfigMapper growthStageConfigMapper;
    /**
     * 积分服务。
     */
    private final PointsService pointsService;
    /**
     * 用户服务。
     */
    private final UserService userService;

    /**
     * 查询我的宠物资料。
     *
     * @param userId 用户 ID。
     * @return 处理结果。
     */
    public PetProfileResponse getProfile(Long userId) {
        PetUser pet = getPet(userId);
        return PetProfileResponse.builder()
                .hasPet(pet != null)
                .petInfo(pet != null ? toPetInfo(pet) : null)
                .build();
    }

    /**
     * 查询首次领养选项。
     *
     * @return 处理结果。
     */
    public PetInitOptionsResponse getInitOptions() {
        List<PetTypeConfig> petTypes = petTypeConfigMapper.selectList(
                new LambdaQueryWrapper<PetTypeConfig>()
                        .eq(PetTypeConfig::getEnabled, 1)
                        .orderByAsc(PetTypeConfig::getSortNo)
                        .orderByAsc(PetTypeConfig::getId));
        List<PetColorConfig> colors = petColorConfigMapper.selectList(
                new LambdaQueryWrapper<PetColorConfig>()
                        .eq(PetColorConfig::getEnabled, 1)
                        .orderByAsc(PetColorConfig::getSortNo)
                        .orderByAsc(PetColorConfig::getId));
        List<PetGrowthStageConfig> stages = growthStageConfigMapper.selectList(
                new LambdaQueryWrapper<PetGrowthStageConfig>()
                        .eq(PetGrowthStageConfig::getEnabled, 1)
                        .orderByAsc(PetGrowthStageConfig::getSortNo)
                        .orderByAsc(PetGrowthStageConfig::getStageNo));
        if (petTypes.isEmpty()) {
            petTypes = getDefaultPetTypes();
        }
        if (colors.isEmpty()) {
            colors = getDefaultPetColors();
        }
        if (stages.isEmpty()) {
            stages = getDefaultGrowthStages(colors, petTypes);
        }

        Map<String, List<PetColorConfig>> colorsByType = colors.stream()
                .collect(Collectors.groupingBy(PetColorConfig::getPetType));
        Map<String, List<PetGrowthStageConfig>> stagesByTypeColor = stages.stream()
                .collect(Collectors.groupingBy(stage -> stage.getPetType() + ":" + stage.getColorCode()));

        List<PetTypeOptionResponse> typeOptions = petTypes.stream()
                .map(type -> PetTypeOptionResponse.builder()
                        .petType(type.getPetType())
                        .petTypeName(type.getPetName())
                        .description(type.getDescription())
                        .defaultColorCode(type.getDefaultColorCode())
                        .colors(colorsByType.getOrDefault(type.getPetType(), List.of()).stream()
                                .map(color -> PetColorOptionResponse.builder()
                                        .colorCode(color.getColorCode())
                                        .colorName(color.getColorName())
                                        .colorHex(color.getColorHex())
                                        .assetKey(color.getAssetKey())
                                        .stagePreviewList(stagesByTypeColor
                                                .getOrDefault(color.getPetType() + ":" + color.getColorCode(), List.of())
                                                .stream()
                                                .map(this::toGrowthStage)
                                                .toList())
                                        .build())
                                .toList())
                        .build())
                .toList();
        return PetInitOptionsResponse.builder().petTypes(typeOptions).build();
    }

    /**
     * 首次领养宠物。
     *
     * @param userId 用户 ID。
     * @param request 请求参数。
     * @return 处理结果。
     */
    @Transactional
    public PetInfoResponse selectPet(Long userId, PetInitSelectRequest request) {
        if (getPet(userId) != null) {
            throw new BusinessException(40004, "已经领养过宠物");
        }

        String petTypeCode = request.getPetType().toUpperCase();
        PetTypeConfig petType = petTypeConfigMapper.selectOne(
                new LambdaQueryWrapper<PetTypeConfig>()
                        .eq(PetTypeConfig::getPetType, petTypeCode)
                        .eq(PetTypeConfig::getEnabled, 1));
        if (petType == null) {
            petType = getDefaultPetType(petTypeCode);
        }
        if (petType == null) {
            throw new BusinessException(40001, "宠物类型不存在");
        }
        String colorCode = request.getColorCode() == null || request.getColorCode().isBlank()
                ? petType.getDefaultColorCode()
                : request.getColorCode().toUpperCase();
        PetColorConfig color = null;
        if (colorCode != null && !colorCode.isBlank()) {
            color = petColorConfigMapper.selectOne(
                    new LambdaQueryWrapper<PetColorConfig>()
                            .eq(PetColorConfig::getPetType, petTypeCode)
                            .eq(PetColorConfig::getColorCode, colorCode)
                            .eq(PetColorConfig::getEnabled, 1));
        }
        if (color == null) {
            color = getDefaultPetColor(petTypeCode, colorCode);
        }
        if (color == null) {
            color = petColorConfigMapper.selectList(
                            new LambdaQueryWrapper<PetColorConfig>()
                                    .eq(PetColorConfig::getPetType, petTypeCode)
                                    .eq(PetColorConfig::getEnabled, 1)
                                    .orderByAsc(PetColorConfig::getSortNo)
                                    .orderByAsc(PetColorConfig::getId))
                    .stream()
                    .findFirst()
                    .orElse(null);
        }
        if (color == null) {
            color = getDefaultPetColors().stream()
                    .filter(item -> item.getPetType().equals(petTypeCode))
                    .findFirst()
                    .orElse(null);
        }
        if (color == null) {
            throw new BusinessException(40003, "颜色不可用");
        }
        colorCode = color.getColorCode();
        PetGrowthStageConfig stage = getStageConfig(petTypeCode, colorCode, 1);
        if (stage == null) {
            throw new BusinessException("宠物成长阶段配置不存在");
        }

        String petName = normalizePetName(request.getPetName(), petType.getPetName());
        LocalDateTime now = LocalDateTime.now();
        PetUser pet = new PetUser();
        pet.setUserId(userId);
        pet.setPetType(petTypeCode);
        pet.setPetColorCode(colorCode);
        pet.setPetAssetKey(stage.getAssetKey());
        pet.setPetName(petName);
        pet.setLevel(1);
        pet.setStageNo(1);
        pet.setExp(0);
        pet.setHunger(INITIAL_PET_STATUS_VALUE);
        pet.setClean(INITIAL_PET_STATUS_VALUE);
        pet.setHappiness(INITIAL_PET_STATUS_VALUE);
        pet.setEnergy(INITIAL_PET_STATUS_VALUE);
        pet.setLoveValue(0);
        pet.setInitTime(now);
        pet.setCreateTime(now);
        pet.setUpdateTime(now);
        petUserMapper.insert(pet);
        return toPetInfo(pet, petType, color, stage);
    }

    /**
     * 查询宠物首页。
     *
     * @param userId 用户 ID。
     * @return 处理结果。
     */
    public PetHomeResponse getHome(Long userId) {
        User user = userService.getUserById(userId);
        PetUser pet = requirePet(userId);
        PetInfoResponse petInfo = toPetInfo(pet);
        return PetHomeResponse.builder()
                .availablePoints(user.getTotalPoints())
                .petInfo(petInfo)
                .benefits(getBenefitList(userId, null))
                .myBenefits(getMyBenefits(userId, null, STATUS_AVAILABLE))
                .build();
    }

    /**
     * 查询宠物权益列表。
     *
     * @param userId 用户 ID。
     * @param benefitType 权益类型。
     * @return 处理结果。
     */
    public PetBenefitListResponse getBenefitList(Long userId, String benefitType) {
        User user = userService.getUserById(userId);
        LambdaQueryWrapper<PetBenefitConfig> query = new LambdaQueryWrapper<PetBenefitConfig>()
                .eq(PetBenefitConfig::getEnabled, 1)
                .orderByAsc(PetBenefitConfig::getSortNo)
                .orderByAsc(PetBenefitConfig::getId);
        if (benefitType != null && !benefitType.isBlank()) {
            query.eq(PetBenefitConfig::getBenefitType, benefitType.toUpperCase());
        }
        List<PetBenefitItemResponse> items = benefitConfigMapper.selectList(query).stream()
                .map(config -> toBenefitItem(userId, user.getTotalPoints(), config))
                .toList();
        return PetBenefitListResponse.builder()
                .availablePoints(user.getTotalPoints())
                .list(items)
                .build();
    }

    /**
     * 兑换宠物权益。
     *
     * @param userId 用户 ID。
     * @param benefitId 权益 ID。
     * @return 处理结果。
     */
    @Transactional(noRollbackFor = BusinessException.class)
    public PetExchangeResponse exchangeBenefit(Long userId, Long benefitId) {
        requirePet(userId);
        PetBenefitConfig config = benefitConfigMapper.selectOne(
                new LambdaQueryWrapper<PetBenefitConfig>()
                        .eq(PetBenefitConfig::getId, benefitId)
                        .last("FOR UPDATE"));
        if (config == null) {
            throw new BusinessException(40001, "权益不存在");
        }
        validateExchange(userId, config);

        String orderNo = generateOrderNo(userId);
        PetBenefitExchangeOrder order = createOrder(userId, config, orderNo);
        exchangeOrderMapper.insert(order);

        try {
            int availablePoints = pointsService.consumePoints(
                    userId,
                    config.getCostPoints(),
                    POINT_TYPE_EXCHANGE,
                    order.getId(),
                    "消耗" + config.getCostPoints() + "积分兑换" + config.getBenefitName() + "，订单号：" + orderNo);
            PetUserBenefit userBenefit = grantBenefit(userId, config);
            reduceStock(config);

            order.setStatus(ORDER_SUCCESS);
            order.setUpdateTime(LocalDateTime.now());
            exchangeOrderMapper.updateById(order);

            return PetExchangeResponse.builder()
                    .orderNo(orderNo)
                    .benefitId(config.getId())
                    .benefitCode(config.getBenefitCode())
                    .benefitName(config.getBenefitName())
                    .benefitType(config.getBenefitType())
                    .costPoints(config.getCostPoints())
                    .availablePoints(availablePoints)
                    .quantity(userBenefit.getQuantity())
                    .build();
        } catch (BusinessException e) {
            order.setStatus(ORDER_FAILED);
            order.setFailReason(e.getMessage());
            order.setUpdateTime(LocalDateTime.now());
            exchangeOrderMapper.updateById(order);
            throw e;
        }
    }

    /**
     * 查询我的宠物权益。
     *
     * @param userId 用户 ID。
     * @param benefitType 权益类型。
     * @param status 状态。
     * @return 处理结果。
     */
    public PetUserBenefitListResponse getMyBenefits(Long userId, String benefitType, Integer status) {
        LambdaQueryWrapper<PetUserBenefit> query = new LambdaQueryWrapper<PetUserBenefit>()
                .eq(PetUserBenefit::getUserId, userId)
                .orderByDesc(PetUserBenefit::getUpdateTime)
                .orderByDesc(PetUserBenefit::getId);
        if (benefitType != null && !benefitType.isBlank()) {
            query.eq(PetUserBenefit::getBenefitType, benefitType.toUpperCase());
        }
        if (status != null) {
            query.eq(PetUserBenefit::getStatus, status);
        }
        List<PetUserBenefitResponse> list = userBenefitMapper.selectList(query).stream()
                .map(this::toUserBenefit)
                .toList();
        return PetUserBenefitListResponse.builder().list(list).build();
    }

    /**
     * 使用宠物权益。
     *
     * @param userId 用户 ID。
     * @param userBenefitId 用户权益 ID。
     * @return 处理结果。
     */
    @Transactional
    public PetUseBenefitResponse useBenefit(Long userId, Long userBenefitId) {
        PetUserBenefit userBenefit = userBenefitMapper.selectOne(
                new LambdaQueryWrapper<PetUserBenefit>()
                        .eq(PetUserBenefit::getId, userBenefitId)
                        .eq(PetUserBenefit::getUserId, userId)
                        .last("FOR UPDATE"));
        if (userBenefit == null) {
            throw new BusinessException("权益不存在");
        }
        if (!Integer.valueOf(STATUS_AVAILABLE).equals(userBenefit.getStatus())) {
            throw new BusinessException("权益不可用");
        }

        PetBenefitConfig config = benefitConfigMapper.selectById(userBenefit.getBenefitId());
        if (config == null) {
            throw new BusinessException("权益配置不存在");
        }
        PetUser pet = applyBenefitEffect(userId, config);

        int remainingQuantity = userBenefit.getQuantity();
        if (TYPE_CONSUMABLE.equals(userBenefit.getBenefitType())) {
            if (remainingQuantity <= 0) {
                throw new BusinessException("权益数量不足");
            }
            remainingQuantity--;
            userBenefit.setQuantity(remainingQuantity);
            if (remainingQuantity <= 0) {
                userBenefit.setStatus(STATUS_USED);
            }
            userBenefit.setUpdateTime(LocalDateTime.now());
            userBenefitMapper.updateById(userBenefit);
        }

        insertUseLog(userId, userBenefit, config);
        return PetUseBenefitResponse.builder()
                .benefitCode(config.getBenefitCode())
                .benefitName(config.getBenefitName())
                .remainingQuantity(remainingQuantity)
                .petInfo(toPetInfo(pet))
                .currentHatCode(pet.getCurrentHatCode())
                .currentBedCode(pet.getCurrentBedCode())
                .currentRoomTheme(pet.getCurrentRoomTheme())
                .build();
    }

    private void validateExchange(Long userId, PetBenefitConfig config) {
        if (!Integer.valueOf(1).equals(config.getEnabled())) {
            throw new BusinessException(40002, "权益已下架");
        }
        if (TYPE_PERMANENT.equals(config.getBenefitType()) && hasAvailableBenefit(userId, config.getId())) {
            throw new BusinessException(40004, "已拥有该权益");
        }
        if (config.getStock() != null && config.getStock() <= 0) {
            throw new BusinessException(40005, "库存不足");
        }
        checkExchangeLimit(userId, config);
    }

    private void checkExchangeLimit(Long userId, PetBenefitConfig config) {
        if (config.getExchangeLimitDay() != null) {
            LocalDate today = LocalDate.now();
            Long todayCount = exchangeOrderMapper.selectCount(
                    new LambdaQueryWrapper<PetBenefitExchangeOrder>()
                            .eq(PetBenefitExchangeOrder::getUserId, userId)
                            .eq(PetBenefitExchangeOrder::getBenefitId, config.getId())
                            .eq(PetBenefitExchangeOrder::getStatus, ORDER_SUCCESS)
                            .ge(PetBenefitExchangeOrder::getCreateTime, today.atStartOfDay())
                            .lt(PetBenefitExchangeOrder::getCreateTime, today.plusDays(1).atStartOfDay()));
            if (todayCount >= config.getExchangeLimitDay()) {
                throw new BusinessException(40006, "已达到今日兑换上限");
            }
        }
        if (config.getExchangeLimitTotal() != null) {
            Long totalCount = exchangeOrderMapper.selectCount(
                    new LambdaQueryWrapper<PetBenefitExchangeOrder>()
                            .eq(PetBenefitExchangeOrder::getUserId, userId)
                            .eq(PetBenefitExchangeOrder::getBenefitId, config.getId())
                            .eq(PetBenefitExchangeOrder::getStatus, ORDER_SUCCESS));
            if (totalCount >= config.getExchangeLimitTotal()) {
                throw new BusinessException("已达到总兑换上限");
            }
        }
    }

    private PetUserBenefit grantBenefit(Long userId, PetBenefitConfig config) {
        LocalDateTime now = LocalDateTime.now();
        PetUserBenefit userBenefit = userBenefitMapper.selectOne(
                new LambdaQueryWrapper<PetUserBenefit>()
                        .eq(PetUserBenefit::getUserId, userId)
                        .eq(PetUserBenefit::getBenefitId, config.getId())
                        .eq(PetUserBenefit::getStatus, STATUS_AVAILABLE)
                        .last("FOR UPDATE"));
        if (TYPE_CONSUMABLE.equals(config.getBenefitType()) && userBenefit != null) {
            userBenefit.setQuantity(userBenefit.getQuantity() + 1);
            userBenefit.setUpdateTime(now);
            userBenefitMapper.updateById(userBenefit);
            return userBenefit;
        }
        if (TYPE_PERMANENT.equals(config.getBenefitType()) && userBenefit != null) {
            throw new BusinessException(40004, "已拥有该权益");
        }

        userBenefit = new PetUserBenefit();
        userBenefit.setUserId(userId);
        userBenefit.setBenefitId(config.getId());
        userBenefit.setBenefitCode(config.getBenefitCode());
        userBenefit.setBenefitName(config.getBenefitName());
        userBenefit.setBenefitType(config.getBenefitType());
        userBenefit.setQuantity(1);
        userBenefit.setStatus(STATUS_AVAILABLE);
        userBenefit.setCreateTime(now);
        userBenefit.setUpdateTime(now);
        userBenefitMapper.insert(userBenefit);
        return userBenefit;
    }

    private void reduceStock(PetBenefitConfig config) {
        if (config.getStock() == null) {
            return;
        }
        config.setStock(config.getStock() - 1);
        config.setUpdateTime(LocalDateTime.now());
        benefitConfigMapper.updateById(config);
    }

    private PetUser applyBenefitEffect(Long userId, PetBenefitConfig config) {
        PetUser pet = requirePet(userId);
        switch (config.getEffectType()) {
            case "HUNGER_FULL" -> pet.setHunger(100);
            case "CLEAN_FULL" -> pet.setClean(100);
            case "HAPPINESS_FULL" -> pet.setHappiness(100);
            case "ENERGY_FULL" -> pet.setEnergy(100);
            case "EXP_ADD" -> addExp(pet, config.getEffectValue() != null ? config.getEffectValue() : 0);
            case "HAT_UNLOCK" -> pet.setCurrentHatCode(config.getBenefitCode());
            case "BED_UNLOCK" -> pet.setCurrentBedCode(config.getBenefitCode());
            case "ROOM_THEME_UNLOCK" -> pet.setCurrentRoomTheme(config.getBenefitCode());
            default -> throw new BusinessException("不支持的权益效果");
        }
        pet.setUpdateTime(LocalDateTime.now());
        petUserMapper.updateById(pet);
        return pet;
    }

    private void addExp(PetUser pet, int addValue) {
        int totalExp = Math.max(0, (pet.getLevel() - 1) * 100 + pet.getExp() + addValue);
        int newLevel = Math.min(MAX_LEVEL, totalExp / 100 + 1);
        int newExp = newLevel >= MAX_LEVEL ? 0 : totalExp % 100;
        int newStageNo = calculateStageNo(newLevel);
        pet.setLevel(newLevel);
        pet.setExp(newExp);
        pet.setStageNo(newStageNo);
        PetGrowthStageConfig stage = getStageConfig(pet.getPetType(), pet.getPetColorCode(), newStageNo);
        if (stage != null) {
            pet.setPetAssetKey(stage.getAssetKey());
        }
    }

    private int calculateStageNo(int level) {
        if (level <= 5) {
            return 1;
        }
        if (level <= 15) {
            return 2;
        }
        if (level <= 30) {
            return 3;
        }
        if (level <= 50) {
            return 4;
        }
        return 5;
    }

    private PetUser getPet(Long userId) {
        return petUserMapper.selectOne(
                new LambdaQueryWrapper<PetUser>().eq(PetUser::getUserId, userId));
    }

    private PetUser requirePet(Long userId) {
        PetUser pet = getPet(userId);
        if (pet == null) {
            throw new BusinessException("请先领养宠物");
        }
        return pet;
    }

    private PetBenefitExchangeOrder createOrder(Long userId, PetBenefitConfig config, String orderNo) {
        LocalDateTime now = LocalDateTime.now();
        PetBenefitExchangeOrder order = new PetBenefitExchangeOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setBenefitId(config.getId());
        order.setBenefitCode(config.getBenefitCode());
        order.setBenefitName(config.getBenefitName());
        order.setBenefitType(config.getBenefitType());
        order.setCostPoints(config.getCostPoints());
        order.setStatus(ORDER_PROCESSING);
        order.setCreateTime(now);
        order.setUpdateTime(now);
        return order;
    }

    private void insertUseLog(Long userId, PetUserBenefit userBenefit, PetBenefitConfig config) {
        PetBenefitUseLog log = new PetBenefitUseLog();
        log.setUserId(userId);
        log.setUserBenefitId(userBenefit.getId());
        log.setBenefitId(config.getId());
        log.setBenefitCode(config.getBenefitCode());
        log.setBenefitName(config.getBenefitName());
        log.setBenefitType(config.getBenefitType());
        log.setEffectType(config.getEffectType());
        log.setEffectValue(config.getEffectValue());
        log.setEffectDesc(config.getDescription());
        log.setCreateTime(LocalDateTime.now());
        useLogMapper.insert(log);
    }

    private boolean hasAvailableBenefit(Long userId, Long benefitId) {
        return userBenefitMapper.selectCount(
                new LambdaQueryWrapper<PetUserBenefit>()
                        .eq(PetUserBenefit::getUserId, userId)
                        .eq(PetUserBenefit::getBenefitId, benefitId)
                        .eq(PetUserBenefit::getStatus, STATUS_AVAILABLE)) > 0;
    }

    private PetBenefitItemResponse toBenefitItem(Long userId, int availablePoints, PetBenefitConfig config) {
        PetUserBenefit userBenefit = userBenefitMapper.selectOne(
                new LambdaQueryWrapper<PetUserBenefit>()
                        .eq(PetUserBenefit::getUserId, userId)
                        .eq(PetUserBenefit::getBenefitId, config.getId())
                        .eq(PetUserBenefit::getStatus, STATUS_AVAILABLE)
                        .last("LIMIT 1"));
        boolean owned = userBenefit != null;
        boolean canExchange = availablePoints >= config.getCostPoints()
                && (config.getStock() == null || config.getStock() > 0)
                && (!TYPE_PERMANENT.equals(config.getBenefitType()) || !owned);
        return PetBenefitItemResponse.builder()
                .benefitId(config.getId())
                .benefitCode(config.getBenefitCode())
                .benefitName(config.getBenefitName())
                .benefitType(config.getBenefitType())
                .costPoints(config.getCostPoints())
                .description(config.getDescription())
                .effectType(config.getEffectType())
                .effectValue(config.getEffectValue())
                .iconUrl(config.getIconUrl())
                .owned(owned)
                .quantity(userBenefit != null ? userBenefit.getQuantity() : 0)
                .stock(config.getStock())
                .canExchange(canExchange)
                .build();
    }

    private PetUserBenefitResponse toUserBenefit(PetUserBenefit userBenefit) {
        return PetUserBenefitResponse.builder()
                .userBenefitId(userBenefit.getId())
                .benefitId(userBenefit.getBenefitId())
                .benefitCode(userBenefit.getBenefitCode())
                .benefitName(userBenefit.getBenefitName())
                .benefitType(userBenefit.getBenefitType())
                .quantity(userBenefit.getQuantity())
                .status(userBenefit.getStatus())
                .build();
    }

    private PetInfoResponse toPetInfo(PetUser pet) {
        PetTypeConfig type = petTypeConfigMapper.selectOne(
                new LambdaQueryWrapper<PetTypeConfig>().eq(PetTypeConfig::getPetType, pet.getPetType()));
        PetColorConfig color = petColorConfigMapper.selectOne(
                new LambdaQueryWrapper<PetColorConfig>()
                        .eq(PetColorConfig::getPetType, pet.getPetType())
                        .eq(PetColorConfig::getColorCode, pet.getPetColorCode()));
        PetGrowthStageConfig stage = getStageConfig(
                pet.getPetType(), pet.getPetColorCode(), pet.getStageNo() != null ? pet.getStageNo() : calculateStageNo(pet.getLevel()));
        return toPetInfo(pet, type, color, stage);
    }

    private PetInfoResponse toPetInfo(PetUser pet, PetTypeConfig type, PetColorConfig color, PetGrowthStageConfig stage) {
        return PetInfoResponse.builder()
                .petId(pet.getId())
                .petType(pet.getPetType())
                .petTypeName(type != null ? type.getPetName() : pet.getPetType())
                .petColorCode(pet.getPetColorCode())
                .petColorName(color != null ? color.getColorName() : pet.getPetColorCode())
                .petName(pet.getPetName())
                .level(pet.getLevel())
                .stageNo(pet.getStageNo())
                .stageName(stage != null ? stage.getStageName() : null)
                .petAssetKey(pet.getPetAssetKey())
                .nextStage(getNextStage(pet))
                .exp(pet.getExp())
                .hunger(pet.getHunger())
                .clean(pet.getClean())
                .happiness(pet.getHappiness())
                .energy(pet.getEnergy())
                .loveValue(pet.getLoveValue())
                .currentHatCode(pet.getCurrentHatCode())
                .currentBedCode(pet.getCurrentBedCode())
                .currentRoomTheme(pet.getCurrentRoomTheme())
                .build();
    }

    private PetNextStageResponse getNextStage(PetUser pet) {
        int stageNo = pet.getStageNo() != null ? pet.getStageNo() : calculateStageNo(pet.getLevel());
        if (stageNo >= 5) {
            return null;
        }
        PetGrowthStageConfig nextStage = getStageConfig(pet.getPetType(), pet.getPetColorCode(), stageNo + 1);
        if (nextStage == null) {
            return null;
        }
        return PetNextStageResponse.builder()
                .stageNo(nextStage.getStageNo())
                .stageName(nextStage.getStageName())
                .needLevel(nextStage.getMinLevel())
                .remainLevel(Math.max(0, nextStage.getMinLevel() - pet.getLevel()))
                .build();
    }

    private PetGrowthStageConfig getStageConfig(String petType, String colorCode, int stageNo) {
        if (petType == null || colorCode == null) {
            return null;
        }
        PetGrowthStageConfig stage = growthStageConfigMapper.selectOne(
                new LambdaQueryWrapper<PetGrowthStageConfig>()
                        .eq(PetGrowthStageConfig::getPetType, petType)
                        .eq(PetGrowthStageConfig::getColorCode, colorCode)
                        .eq(PetGrowthStageConfig::getStageNo, stageNo)
                        .eq(PetGrowthStageConfig::getEnabled, 1));
        return stage != null ? stage : getDefaultGrowthStage(petType, colorCode, stageNo);
    }

    private PetGrowthStageResponse toGrowthStage(PetGrowthStageConfig stage) {
        return PetGrowthStageResponse.builder()
                .stageNo(stage.getStageNo())
                .stageName(stage.getStageName())
                .minLevel(stage.getMinLevel())
                .maxLevel(stage.getMaxLevel())
                .assetKey(stage.getAssetKey())
                .previewAssetKey(stage.getPreviewAssetKey())
                .description(stage.getDescription())
                .build();
    }

    private String normalizePetName(String petName, String defaultName) {
        String normalized = petName == null || petName.isBlank() ? defaultName : petName.trim();
        if (normalized.length() > 20) {
            throw new BusinessException(40005, "宠物名称不能超过20个字符");
        }
        return normalized;
    }

    private List<PetTypeConfig> getDefaultPetTypes() {
        return List.of(
                buildPetType("RABBIT", "小白兔", "温柔可爱的小白兔", "WHITE", 1),
                buildPetType("DINOSAUR", "小恐龙", "勇敢又特别的小恐龙", "GREEN", 2),
                buildPetType("ANGELWOMON", "天女兽", "温柔又闪耀的天使系伙伴", "GOLD", 3),
                buildPetType("ANGEMON", "天使兽", "正直又可靠的神圣系伙伴", "GOLD", 4)
        );
    }

    private PetTypeConfig getDefaultPetType(String petType) {
        return getDefaultPetTypes().stream()
                .filter(item -> item.getPetType().equals(petType))
                .findFirst()
                .orElse(null);
    }

    private PetTypeConfig buildPetType(String petType, String petName, String description, String defaultColorCode, int sortNo) {
        PetTypeConfig config = new PetTypeConfig();
        config.setPetType(petType);
        config.setPetName(petName);
        config.setDescription(description);
        config.setDefaultColorCode(defaultColorCode);
        config.setSortNo(sortNo);
        config.setEnabled(1);
        return config;
    }

    private List<PetColorConfig> getDefaultPetColors() {
        return List.of(
                buildPetColor("RABBIT", "WHITE", "白色", "#FFFFFF", "rabbit_white", 1),
                buildPetColor("RABBIT", "PINK", "粉色", "#FFB6C1", "rabbit_pink", 2),
                buildPetColor("RABBIT", "GRAY", "灰色", "#BFC3C7", "rabbit_gray", 3),
                buildPetColor("DINOSAUR", "GREEN", "绿色", "#67C23A", "dinosaur_green", 1),
                buildPetColor("DINOSAUR", "BLUE", "蓝色", "#409EFF", "dinosaur_blue", 2),
                buildPetColor("DINOSAUR", "PURPLE", "紫色", "#9B59B6", "dinosaur_purple", 3),
                buildPetColor("ANGELWOMON", "GOLD", "金色", "#F6C85F", "angelwomon_gold", 1),
                buildPetColor("ANGELWOMON", "WHITE", "白色", "#FFFFFF", "angelwomon_white", 2),
                buildPetColor("ANGELWOMON", "PINK", "粉色", "#FF9ECF", "angelwomon_pink", 3),
                buildPetColor("ANGEMON", "GOLD", "金色", "#F6C85F", "angemon_gold", 1),
                buildPetColor("ANGEMON", "WHITE", "白色", "#FFFFFF", "angemon_white", 2),
                buildPetColor("ANGEMON", "BLUE", "蓝色", "#7DB9FF", "angemon_blue", 3)
        );
    }

    private PetColorConfig getDefaultPetColor(String petType, String colorCode) {
        return getDefaultPetColors().stream()
                .filter(item -> item.getPetType().equals(petType) && item.getColorCode().equals(colorCode))
                .findFirst()
                .orElse(null);
    }

    private PetColorConfig buildPetColor(
            String petType, String colorCode, String colorName, String colorHex, String assetKey, int sortNo) {
        PetColorConfig config = new PetColorConfig();
        config.setPetType(petType);
        config.setColorCode(colorCode);
        config.setColorName(colorName);
        config.setColorHex(colorHex);
        config.setAssetKey(assetKey);
        config.setSortNo(sortNo);
        config.setEnabled(1);
        return config;
    }

    private List<PetGrowthStageConfig> getDefaultGrowthStages(List<PetColorConfig> colors, List<PetTypeConfig> petTypes) {
        Map<String, String> typeNames = petTypes.stream()
                .collect(Collectors.toMap(PetTypeConfig::getPetType, PetTypeConfig::getPetName, (left, right) -> left));
        List<PetGrowthStageConfig> stages = new ArrayList<>();
        for (PetColorConfig color : colors) {
            String petName = typeNames.getOrDefault(color.getPetType(), "宠物");
            for (int stageNo = 1; stageNo <= 5; stageNo++) {
                stages.add(buildGrowthStage(
                        color.getPetType(),
                        color.getColorCode(),
                        color.getAssetKey(),
                        petName,
                        stageNo));
            }
        }
        return stages;
    }

    private PetGrowthStageConfig getDefaultGrowthStage(String petType, String colorCode, int stageNo) {
        PetColorConfig color = getDefaultPetColor(petType, colorCode);
        PetTypeConfig type = getDefaultPetType(petType);
        if (color == null || type == null || stageNo < 1 || stageNo > 5) {
            return null;
        }
        return buildGrowthStage(petType, colorCode, color.getAssetKey(), type.getPetName(), stageNo);
    }

    private PetGrowthStageConfig buildGrowthStage(
            String petType, String colorCode, String baseAssetKey, String petName, int stageNo) {
        PetGrowthStageConfig stage = new PetGrowthStageConfig();
        stage.setPetType(petType);
        stage.setColorCode(colorCode);
        stage.setStageNo(stageNo);
        stage.setStageName(switch (stageNo) {
            case 1 -> "幼年期";
            case 2 -> "成长期";
            case 3 -> "进阶期";
            case 4 -> "成熟期";
            default -> "完全体";
        });
        stage.setMinLevel(switch (stageNo) {
            case 1 -> 1;
            case 2 -> 6;
            case 3 -> 16;
            case 4 -> 31;
            default -> 51;
        });
        stage.setMaxLevel(switch (stageNo) {
            case 1 -> 5;
            case 2 -> 15;
            case 3 -> 30;
            case 4 -> 50;
            default -> 70;
        });
        stage.setAssetKey(baseAssetKey + "_stage_" + stageNo);
        stage.setPreviewAssetKey(baseAssetKey + "_stage_" + stageNo + "_preview");
        stage.setDescription(switch (stageNo) {
            case 1 -> petName + "刚来到你身边，正是小小的幼年期";
            case 2 -> petName + "进入成长期，变得更加活泼";
            case 3 -> petName + "来到进阶期，外观变化更明显";
            case 4 -> petName + "进入成熟期，更完整也更有特色";
            default -> petName + "成长为完全体，已经是最棒的伙伴";
        });
        stage.setEnabled(1);
        stage.setSortNo(stageNo);
        return stage;
    }

    private String generateOrderNo(Long userId) {
        return "PET_EXCHANGE_" + userId + "_" + LocalDateTime.now().format(ORDER_TIME_FORMATTER);
    }
}
