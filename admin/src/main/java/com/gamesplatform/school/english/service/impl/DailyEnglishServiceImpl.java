package com.gamesplatform.school.english.service.impl;

import com.gamesplatform.school.english.service.DailyEnglishService;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gamesplatform.common.BusinessException;
import com.gamesplatform.school.english.dto.DailyEnglishGeneratedContent;
import com.gamesplatform.school.english.dto.DailyEnglishItemResponse;
import com.gamesplatform.school.english.dto.DailyEnglishResponse;
import com.gamesplatform.school.english.dto.DailyEnglishTaskCompletionRequest;
import com.gamesplatform.school.english.dto.DailyEnglishTaskCompletionResponse;
import com.gamesplatform.school.english.dto.DailyEnglishTaskStatusResponse;
import com.gamesplatform.school.english.entity.DailyEnglishConfig;
import com.gamesplatform.school.english.entity.DailyEnglishPractice;
import com.gamesplatform.school.english.mapper.DailyEnglishConfigMapper;
import com.gamesplatform.school.english.mapper.DailyEnglishPracticeMapper;
import com.gamesplatform.system.points.service.PointsService;
import com.gamesplatform.system.user.service.UserService;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 每日英语口语练习服务。
 */
@Slf4j
@Service
public class DailyEnglishServiceImpl implements DailyEnglishService {

    /**
     * 每日英语单例配置记录的固定主键。
     */
    private static final long CONFIG_ID = 1L;

    /**
     * 每日英语练习 Redis 缓存键前缀。
     */
    private static final String CACHE_KEY_PREFIX = "daily-english:practice:";

    /**
     * Redis 缓存有效期；缓存键包含日期，跨日后不会读取旧内容。
     */
    private static final Duration CACHE_TTL = Duration.ofHours(25);

    /**
     * 完成每日英语全部跟读任务的奖励积分。
     */
    private static final int TASK_REWARD_POINTS = 20;

    /**
     * 用户每日英语跟读任务完成标记的 Redis 键前缀。
     */
    private static final String TASK_COMPLETION_KEY_PREFIX = "daily-english:task-completed:";

    /**
     * 每日英语生成配置数据访问组件。
     */
    @Schema(description = "每日英语生成配置数据访问组件")
    private final DailyEnglishConfigMapper configMapper;

    /**
     * 每日英语练习缓存数据访问组件。
     */
    @Schema(description = "每日英语练习缓存数据访问组件")
    private final DailyEnglishPracticeMapper practiceMapper;

    /**
     * 练习响应与缓存 JSON 之间的序列化组件。
     */
    @Schema(description = "练习响应与缓存 JSON 之间的序列化组件")
    private final ObjectMapper objectMapper;

    /**
     * 每日英语练习 Redis 缓存访问组件。
     */
    @Schema(description = "每日英语练习 Redis 缓存访问组件")
    private final StringRedisTemplate redisTemplate;

    /**
     * 积分业务服务。
     */
    @Schema(description = "积分业务服务")
    private final PointsService pointsService;

    /**
     * 用户业务服务。
     */
    @Schema(description = "用户业务服务")
    private final UserService userService;

    /**
     * 调用大模型生成口语练习的聊天客户端。
     */
    @Schema(description = "调用大模型生成口语练习的聊天客户端")
    private final ChatClient chatClient;

    /**
     * 大模型服务 API Key，用于判断生成能力是否已经配置。
     */
    @Schema(description = "大模型服务 API Key，用于判断生成能力是否已经配置")
    private final String apiKey;

    /**
     * 创建每日英语口语练习服务。
     *
     * @param configMapper 每日英语生成配置数据访问组件。
     * @param practiceMapper 每日英语练习缓存数据访问组件。
     * @param objectMapper JSON 序列化组件。
     * @param redisTemplate Redis 字符串缓存访问组件。
     * @param pointsService 积分业务服务。
     * @param userService 用户业务服务。
     * @param chatClientBuilder 大模型聊天客户端构建器。
     * @param apiKey 大模型服务 API Key。
     */
    public DailyEnglishServiceImpl(
            DailyEnglishConfigMapper configMapper,
            DailyEnglishPracticeMapper practiceMapper,
            ObjectMapper objectMapper,
            StringRedisTemplate redisTemplate,
            PointsService pointsService,
            UserService userService,
            ChatClient.Builder chatClientBuilder,
            @Value("${llm.api-key:}") String apiKey) {
        this.configMapper = configMapper;
        this.practiceMapper = practiceMapper;
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.pointsService = pointsService;
        this.userService = userService;
        this.chatClient = chatClientBuilder.build();
        this.apiKey = apiKey;
    }

    /**
     * 获取当天适用于当前配置年级的英语口语练习。
     * 已生成的内容优先从 Redis 读取，未命中时查询数据库并回填 Redis；
     * 两级缓存均未命中时调用大模型生成并保存。
     *
     * @return 当天的英语口语练习。
     */
    @Override
    public DailyEnglishResponse getTodayPractice() {
        DailyEnglishConfig config = configMapper.selectById(CONFIG_ID);
        if (config == null) {
            throw new BusinessException("每日英语配置不存在，请联系管理员");
        }
        LocalDate today = LocalDate.now();
        String cacheKey = cacheKey(today, config);
        DailyEnglishResponse redisCached = readRedisCache(cacheKey);
        if (redisCached != null) {
            return redisCached;
        }
        DailyEnglishPractice cached = findPractice(today, config.getGradeLevel());
        if (cached != null) {
            DailyEnglishResponse response = readCached(cached);
            writeRedisCache(cacheKey, response);
            return response;
        }
        if (apiKey == null || apiKey.isBlank() || "not-configured".equals(apiKey)) {
            throw new BusinessException("每日英语大模型尚未配置，请设置 LLM_API_KEY");
        }

        DailyEnglishResponse generated = generate(config, today);
        DailyEnglishPractice practice = new DailyEnglishPractice();
        practice.setPracticeDate(today);
        practice.setGradeLevel(config.getGradeLevel());
        practice.setContentJson(writeContent(generated));
        practice.setCreatedAt(LocalDateTime.now());
        try {
            practiceMapper.insert(practice);
            writeRedisCache(cacheKey, generated);
            return generated;
        } catch (DuplicateKeyException ignored) {
            DailyEnglishPractice existing = findPractice(today, config.getGradeLevel());
            DailyEnglishResponse response = existing == null ? generated : readCached(existing);
            writeRedisCache(cacheKey, response);
            return response;
        }
    }

    /**
     * 查询当前用户当天的每日英语跟读任务状态。
     *
     * @param userId 当前用户 ID。
     * @return 当天任务状态及可获得的奖励积分。
     */
    @Override
    public DailyEnglishTaskStatusResponse getTodayTaskStatus(Long userId) {
        return new DailyEnglishTaskStatusResponse(
                hasCompletedTask(userId, LocalDate.now()),
                TASK_REWARD_POINTS);
    }

    /**
     * 校验并结算当前用户当天的每日英语跟读任务。
     * 每位用户每天只能首次完成时获得一次积分奖励。
     *
     * @param userId 当前用户 ID。
     * @param request 客户端已完成的跟读内容。
     * @return 跟读任务结算结果。
     */
    @Override
    public DailyEnglishTaskCompletionResponse completeTodayTask(
            Long userId,
            DailyEnglishTaskCompletionRequest request) {
        LocalDate today = LocalDate.now();
        DailyEnglishResponse practice = getTodayPractice();
        validateCompletedItems(practice.getItems(), request.getCompletedItems());
        String completionKey = completionCacheKey(userId, today);
        if (!claimTaskCompletion(completionKey, today)) {
            return completedResponse(userId);
        }

        try {
            int totalPoints = pointsService.awardPoints(
                    userId,
                    TASK_REWARD_POINTS,
                    "DAILY_ENGLISH_COMPLETE",
                    today.toEpochDay(),
                    "完成每日英语跟读任务");
            return new DailyEnglishTaskCompletionResponse(true, TASK_REWARD_POINTS, totalPoints);
        } catch (RuntimeException exception) {
            releaseTaskCompletion(completionKey);
            throw exception;
        }
    }

    /**
     * 校验客户端提交的跟读内容是否完整覆盖当天全部任务。
     *
     * @param expectedItems 当天生成的全部跟读任务。
     * @param completedItems 客户端提交的已完成跟读内容。
     */
    private void validateCompletedItems(
            List<DailyEnglishItemResponse> expectedItems,
            List<String> completedItems) {
        if (expectedItems == null || completedItems == null
                || expectedItems.size() != completedItems.size()) {
            throw new BusinessException("请完成当天全部英语跟读任务");
        }
        List<String> remaining = new ArrayList<>(completedItems.stream()
                .map(this::normalizeTaskText)
                .toList());
        for (DailyEnglishItemResponse item : expectedItems) {
            if (item == null || !remaining.remove(normalizeTaskText(item.getText()))) {
                throw new BusinessException("跟读任务与当天练习内容不一致");
            }
        }
    }

    /**
     * 查询用户是否已完成指定日期的跟读任务。
     *
     * @param userId 用户 ID。
     * @param practiceDate 跟读任务所属自然日。
     * @return 已完成时返回 {@code true}，否则返回 {@code false}。
     */
    private boolean hasCompletedTask(Long userId, LocalDate practiceDate) {
        try {
            return redisTemplate.hasKey(completionCacheKey(userId, practiceDate));
        } catch (RuntimeException exception) {
            throw new BusinessException("每日英语任务状态暂时不可用，请稍后重试");
        }
    }

    /**
     * 原子占用当天跟读任务奖励资格，并设置标记在当天结束时失效。
     *
     * @param completionKey 用户当天的任务完成缓存键。
     * @param practiceDate 跟读任务所属自然日。
     * @return 首次占用成功时返回 {@code true}，已完成时返回 {@code false}。
     */
    private boolean claimTaskCompletion(String completionKey, LocalDate practiceDate) {
        try {
            Boolean claimed = redisTemplate.opsForValue().setIfAbsent(
                    completionKey,
                    "1",
                    completionTtl(practiceDate));
            return Boolean.TRUE.equals(claimed);
        } catch (RuntimeException exception) {
            throw new BusinessException("每日英语任务结算暂时不可用，请稍后重试");
        }
    }

    /**
     * 积分发放失败时释放任务完成标记，允许用户再次发起结算。
     *
     * @param completionKey 用户当天的任务完成缓存键。
     */
    private void releaseTaskCompletion(String completionKey) {
        try {
            redisTemplate.delete(completionKey);
        } catch (RuntimeException exception) {
            log.error("每日英语积分发放失败后释放任务标记失败：{}", completionKey, exception);
        }
    }

    /**
     * 构建用户当天的跟读任务完成缓存键。
     *
     * @param userId 用户 ID。
     * @param practiceDate 跟读任务所属自然日。
     * @return 用户当天的 Redis 任务完成键。
     */
    private String completionCacheKey(Long userId, LocalDate practiceDate) {
        return TASK_COMPLETION_KEY_PREFIX + practiceDate + ":user:" + userId;
    }

    /**
     * 计算任务完成标记距离当天 23:59:59 的有效时长。
     *
     * @param practiceDate 跟读任务所属自然日。
     * @return Redis 完成标记有效时长。
     */
    private Duration completionTtl(LocalDate practiceDate) {
        Duration ttl = Duration.between(
                LocalDateTime.now(),
                practiceDate.atTime(LocalTime.of(23, 59, 59)));
        return ttl.isNegative() || ttl.isZero() ? Duration.ofSeconds(1) : ttl;
    }

    /**
     * 构建任务已经完成且本次不再重复奖励的响应。
     *
     * @param userId 用户 ID。
     * @return 未重复发放积分的任务结算响应。
     */
    private DailyEnglishTaskCompletionResponse completedResponse(Long userId) {
        int totalPoints = userService.getUserById(userId).getTotalPoints();
        return new DailyEnglishTaskCompletionResponse(false, 0, totalPoints);
    }

    /**
     * 规范化跟读任务文本，用于服务端完整性校验。
     *
     * @param value 待规范化的跟读文本。
     * @return 转为小写并合并空白后的文本。
     */
    private String normalizeTaskText(String value) {
        return value == null ? "" : value.trim().toLowerCase().replaceAll("\\s+", " ");
    }

    /**
     * 根据管理配置调用大模型生成指定日期的结构化口语练习。
     *
     * @param config 每日英语生成配置。
     * @param date 练习内容对应的自然日。
     * @return 清洗后的每日英语口语练习。
     */
    private DailyEnglishResponse generate(DailyEnglishConfig config, LocalDate date) {
        String gradeLabel = gradeLabel(config.getGradeLevel());
        String skill = config.getSkillMarkdown() == null || config.getSkillMarkdown().isBlank()
                ? "无额外 Skill，使用适合该年级的中国小学英语课程难度。"
                : config.getSkillMarkdown();
        String prompt = """
                请为中国小学%s学生生成今天的英语口语跟读练习。
                要求：
                1. 生成 5 个单词和 3 个日常短句，共 8 项。
                2. type 只能是 WORD 或 SENTENCE；text 只写英文；translation 写简洁中文。
                3. phonetic：单词写常见音标，短句可留空；tip 写一句简短的发音提示。
                4. 内容积极、自然、适龄，不重复，短句不超过 10 个英文单词。
                5. title 使用简短中文主题名。

                管理员补充 Skill（Markdown）：
                %s
                """.formatted(gradeLabel, skill);
        try {
            DailyEnglishGeneratedContent content = chatClient.prompt()
                    .system("你是一位耐心的小学英语口语老师，请严格返回要求的结构化内容。")
                    .user(prompt)
                    .call()
                    .entity(DailyEnglishGeneratedContent.class);
            List<DailyEnglishItemResponse> items = sanitizeItems(content);
            return new DailyEnglishResponse(
                    date,
                    config.getGradeLevel(),
                    gradeLabel,
                    content.getTitle() == null || content.getTitle().isBlank() ? "今日口语练习" : content.getTitle(),
                    items);
        } catch (Exception exception) {
            throw new BusinessException("每日英语生成失败，请检查大模型配置后重试");
        }
    }

    /**
     * 清洗大模型返回的练习项，统一类型、去除空白并限制最大数量。
     *
     * @param content 大模型返回的结构化内容。
     * @return 可直接返回给客户端的有效练习项列表。
     */
    private List<DailyEnglishItemResponse> sanitizeItems(DailyEnglishGeneratedContent content) {
        if (content == null || content.getItems() == null) {
            throw new IllegalStateException("模型未返回练习内容");
        }
        List<DailyEnglishItemResponse> result = new ArrayList<>();
        for (DailyEnglishItemResponse item : content.getItems()) {
            if (item == null || item.getText() == null || item.getText().isBlank()) {
                continue;
            }
            String type = "SENTENCE".equalsIgnoreCase(item.getType()) ? "SENTENCE" : "WORD";
            result.add(new DailyEnglishItemResponse(
                    type,
                    item.getText().trim(),
                    valueOrEmpty(item.getPhonetic()),
                    valueOrEmpty(item.getTranslation()),
                    valueOrEmpty(item.getTip())));
            if (result.size() == 12) {
                break;
            }
        }
        if (result.isEmpty()) {
            throw new IllegalStateException("模型返回的练习内容为空");
        }
        return result;
    }

    /**
     * 查询指定日期和年级的每日英语练习缓存。
     *
     * @param date 练习内容对应的自然日。
     * @param gradeLevel 练习适用年级。
     * @return 找到的缓存记录；不存在时返回 {@code null}。
     */
    private DailyEnglishPractice findPractice(LocalDate date, Integer gradeLevel) {
        return practiceMapper.selectOne(new LambdaQueryWrapper<DailyEnglishPractice>()
                .eq(DailyEnglishPractice::getPracticeDate, date)
                .eq(DailyEnglishPractice::getGradeLevel, gradeLevel)
                .last("LIMIT 1"));
    }

    /**
     * 从 Redis 读取每日英语练习。Redis 不可用或缓存内容无效时返回未命中，
     * 由数据库缓存继续提供服务。
     *
     * @param key Redis 缓存键。
     * @return 缓存中的每日英语响应；未命中或读取失败时返回 {@code null}。
     */
    private DailyEnglishResponse readRedisCache(String key) {
        try {
            String content = redisTemplate.opsForValue().get(key);
            if (content == null || content.isBlank()) {
                return null;
            }
            return objectMapper.readValue(content, DailyEnglishResponse.class);
        } catch (JsonProcessingException exception) {
            log.warn("每日英语 Redis 缓存内容无效，已忽略缓存：{}", key);
            return null;
        } catch (RuntimeException exception) {
            log.warn("读取每日英语 Redis 缓存失败，已降级到数据库：{}", key, exception);
            return null;
        }
    }

    /**
     * 将每日英语响应写入 Redis。写入失败时仅记录日志，不影响正常响应。
     *
     * @param key Redis 缓存键。
     * @param response 每日英语响应。
     */
    private void writeRedisCache(String key, DailyEnglishResponse response) {
        try {
            String content = objectMapper.writeValueAsString(response);
            redisTemplate.opsForValue().set(key, content, CACHE_TTL);
        } catch (JsonProcessingException | RuntimeException exception) {
            log.warn("写入每日英语 Redis 缓存失败，数据库缓存仍可正常使用：{}", key, exception);
        }
    }

    /**
     * 构建包含日期、年级和配置指纹的每日英语 Redis 缓存键。
     * 配置指纹可确保缓存清理失败时，更新后的配置也不会读取旧内容。
     *
     * @param date 练习日期。
     * @param config 每日英语生成配置。
     * @return Redis 缓存键。
     */
    private String cacheKey(LocalDate date, DailyEnglishConfig config) {
        int fingerprint = Objects.hash(
                config.getGradeLevel(),
                valueOrEmpty(config.getSkillMarkdown()),
                config.getUpdatedAt());
        return CACHE_KEY_PREFIX + date
                + ":grade:" + config.getGradeLevel()
                + ":config:" + Integer.toUnsignedString(fingerprint, 16);
    }

    /**
     * 将数据库缓存的 JSON 内容还原为每日英语响应。
     * 缓存损坏时删除无效记录，允许客户端后续重新触发生成。
     *
     * @param practice 每日英语练习缓存记录。
     * @return 反序列化后的每日英语响应。
     */
    private DailyEnglishResponse readCached(DailyEnglishPractice practice) {
        try {
            return objectMapper.readValue(practice.getContentJson(), DailyEnglishResponse.class);
        } catch (JsonProcessingException exception) {
            practiceMapper.deleteById(practice.getId());
            throw new BusinessException("每日英语缓存解析失败，请重新展开后再试");
        }
    }

    /**
     * 将每日英语响应序列化为可持久化的 JSON 内容。
     *
     * @param response 每日英语响应。
     * @return 序列化后的 JSON 字符串。
     */
    private String writeContent(DailyEnglishResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("每日英语内容保存失败");
        }
    }

    /**
     * 将数值年级转换为小学年级中文名称，并将异常值限制在一年级至六年级。
     *
     * @param gradeLevel 数值年级。
     * @return 小学年级中文名称。
     */
    private String gradeLabel(Integer gradeLevel) {
        int grade = gradeLevel == null ? 1 : Math.max(1, Math.min(6, gradeLevel));
        return "小学" + switch (grade) {
            case 1 -> "一年级";
            case 2 -> "二年级";
            case 3 -> "三年级";
            case 4 -> "四年级";
            case 5 -> "五年级";
            default -> "六年级";
        };
    }

    /**
     * 清理可空文本；空值转换为空字符串，非空值移除首尾空白。
     *
     * @param value 待清理文本。
     * @return 清理后的非空文本。
     */
    private String valueOrEmpty(String value) {
        return value == null ? "" : value.trim();
    }
}
