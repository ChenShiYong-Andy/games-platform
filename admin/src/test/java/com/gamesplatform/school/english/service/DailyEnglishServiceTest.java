package com.gamesplatform.school.english.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gamesplatform.school.english.dto.DailyEnglishItemResponse;
import com.gamesplatform.school.english.dto.DailyEnglishResponse;
import com.gamesplatform.school.english.dto.DailyEnglishTaskCompletionRequest;
import com.gamesplatform.school.english.dto.DailyEnglishTaskCompletionResponse;
import com.gamesplatform.school.english.entity.DailyEnglishConfig;
import com.gamesplatform.school.english.entity.DailyEnglishPractice;
import com.gamesplatform.school.english.mapper.DailyEnglishConfigMapper;
import com.gamesplatform.school.english.mapper.DailyEnglishPracticeMapper;
import com.gamesplatform.school.english.service.impl.DailyEnglishServiceImpl;
import com.gamesplatform.system.points.service.PointsService;
import com.gamesplatform.system.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DailyEnglishServiceTest {

    private DailyEnglishConfigMapper configMapper;
    private DailyEnglishPracticeMapper practiceMapper;
    private StringRedisTemplate redisTemplate;
    private ValueOperations<String, String> valueOperations;
    private ObjectMapper objectMapper;
    private PointsService pointsService;
    private UserService userService;
    private DailyEnglishService service;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        configMapper = mock(DailyEnglishConfigMapper.class);
        practiceMapper = mock(DailyEnglishPracticeMapper.class);
        redisTemplate = mock(StringRedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        pointsService = mock(PointsService.class);
        userService = mock(UserService.class);
        ChatClient.Builder chatClientBuilder = mock(ChatClient.Builder.class);
        when(chatClientBuilder.build()).thenReturn(mock(ChatClient.class));
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        service = new DailyEnglishServiceImpl(
                configMapper,
                practiceMapper,
                objectMapper,
                redisTemplate,
                pointsService,
                userService,
                chatClientBuilder,
                "configured-key");

        DailyEnglishConfig config = new DailyEnglishConfig();
        config.setId(1L);
        config.setGradeLevel(3);
        when(configMapper.selectById(1L)).thenReturn(config);
    }

    @Test
    void returnsRedisContentWithoutQueryingDatabaseCache() throws Exception {
        DailyEnglishResponse expected = response();
        when(valueOperations.get(anyString())).thenReturn(objectMapper.writeValueAsString(expected));

        DailyEnglishResponse actual = service.getTodayPractice();

        assertEquals(expected.getTitle(), actual.getTitle());
        assertEquals(expected.getItems().getFirst().getText(), actual.getItems().getFirst().getText());
        verify(practiceMapper, never()).selectOne(any());
    }

    @Test
    void backfillsRedisWhenDatabaseCacheExists() throws Exception {
        DailyEnglishResponse expected = response();
        DailyEnglishPractice practice = new DailyEnglishPractice();
        practice.setId(10L);
        practice.setContentJson(objectMapper.writeValueAsString(expected));
        when(valueOperations.get(anyString())).thenReturn(null);
        when(practiceMapper.selectOne(any())).thenReturn(practice);

        DailyEnglishResponse actual = service.getTodayPractice();

        assertEquals(expected.getTitle(), actual.getTitle());
        verify(valueOperations).set(anyString(), anyString(), eq(Duration.ofHours(25)));
    }

    @Test
    void awardsTwentyPointsAfterAllTasksAreCompleted() throws Exception {
        DailyEnglishResponse expected = response();
        when(valueOperations.get(anyString())).thenReturn(objectMapper.writeValueAsString(expected));
        when(valueOperations.setIfAbsent(anyString(), eq("1"), any(Duration.class))).thenReturn(true);
        when(pointsService.awardPoints(7L, 20, "DAILY_ENGLISH_COMPLETE", LocalDate.now().toEpochDay(),
                "完成每日英语跟读任务")).thenReturn(120);
        DailyEnglishTaskCompletionRequest request = new DailyEnglishTaskCompletionRequest();
        request.setCompletedItems(List.of("apple"));

        DailyEnglishTaskCompletionResponse actual = service.completeTodayTask(7L, request);

        assertEquals(true, actual.isNewlyCompleted());
        assertEquals(20, actual.getPointsEarned());
        assertEquals(120, actual.getTotalPoints());
        verify(pointsService).awardPoints(7L, 20, "DAILY_ENGLISH_COMPLETE", LocalDate.now().toEpochDay(),
                "完成每日英语跟读任务");
        ArgumentCaptor<Duration> ttlCaptor = ArgumentCaptor.forClass(Duration.class);
        verify(valueOperations).setIfAbsent(anyString(), eq("1"), ttlCaptor.capture());
        assertTrue(ttlCaptor.getValue().isPositive());
        assertTrue(ttlCaptor.getValue().compareTo(Duration.ofDays(1)) < 0);
    }

    private DailyEnglishResponse response() {
        return new DailyEnglishResponse(
                LocalDate.now(),
                3,
                "小学三年级",
                "今日校园英语",
                List.of(new DailyEnglishItemResponse("WORD", "apple", "/ˈæp.əl/", "苹果", "注意重音")));
    }
}
