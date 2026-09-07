package com.gamesplatform.config;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * 为 Redis 顶层键统一增加系统命名空间前缀的字符串序列化器。
 */
public final class SystemRedisKeySerializer implements RedisSerializer<String> {

    /**
     * Redis 字符串序列化器。
     */
    private final StringRedisSerializer delegate = new StringRedisSerializer();

    /**
     * 规范化后的系统键前缀。
     */
    private final String keyPrefix;

    /**
     * 创建系统 Redis 键序列化器。
     *
     * @param keyPrefix 系统键前缀；非空前缀会自动补充末尾冒号
     */
    public SystemRedisKeySerializer(String keyPrefix) {
        String normalized = keyPrefix == null ? "" : keyPrefix.trim();
        this.keyPrefix = normalized.isEmpty() || normalized.endsWith(":")
                ? normalized
                : normalized + ":";
    }

    /**
     * 将业务键转换为包含系统前缀的 Redis 物理键。
     *
     * @param value 业务键
     * @return Redis 物理键字节；业务键为空时返回 null
     */
    @Override
    public byte[] serialize(String value) {
        if (value == null) {
            return null;
        }
        String physicalKey = value.startsWith(keyPrefix) ? value : keyPrefix + value;
        return delegate.serialize(physicalKey);
    }

    /**
     * 将 Redis 物理键还原为不含系统前缀的业务键。
     *
     * @param bytes Redis 物理键字节
     * @return 业务键；输入为空时返回 null
     */
    @Override
    public String deserialize(byte[] bytes) {
        String physicalKey = delegate.deserialize(bytes);
        if (physicalKey == null || keyPrefix.isEmpty() || !physicalKey.startsWith(keyPrefix)) {
            return physicalKey;
        }
        return physicalKey.substring(keyPrefix.length());
    }
}
