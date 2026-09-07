package com.gamesplatform.config;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 系统 Redis 键前缀序列化器测试。
 */
class SystemRedisKeySerializerTest {

    /**
     * 验证序列化时自动补充冒号并增加系统前缀。
     */
    @Test
    void addsNormalizedSystemPrefixWhenSerializing() {
        SystemRedisKeySerializer serializer = new SystemRedisKeySerializer("games-platform");

        byte[] serialized = serializer.serialize("ranking:total_points");

        assertEquals("games-platform:ranking:total_points", new String(serialized, StandardCharsets.UTF_8));
    }

    /**
     * 验证从 Redis 读取键时移除系统前缀，避免删除键时重复拼接。
     */
    @Test
    void removesSystemPrefixWhenDeserializing() {
        SystemRedisKeySerializer serializer = new SystemRedisKeySerializer("games-platform:");

        String logicalKey = serializer.deserialize(
                "games-platform:daily-english:practice:2026-09-07:*".getBytes(StandardCharsets.UTF_8));

        assertEquals("daily-english:practice:2026-09-07:*", logicalKey);
    }

    /**
     * 验证空键保持为空。
     */
    @Test
    void preservesNullKeys() {
        SystemRedisKeySerializer serializer = new SystemRedisKeySerializer("games-platform");

        assertNull(serializer.serialize(null));
        assertNull(serializer.deserialize(null));
    }
}
