package com.gamesplatform.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 客户端及系统键命名空间配置。
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 创建统一使用系统键前缀的 Redis 字符串操作模板。
     *
     * @param connectionFactory Redis 连接工厂
     * @param keyPrefix 系统级 Redis 键前缀
     * @return Redis 字符串操作模板
     */
    @Bean
    public StringRedisTemplate stringRedisTemplate(
            RedisConnectionFactory connectionFactory,
            @Value("${system.redis.key-prefix:games-platform}") String keyPrefix) {
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new SystemRedisKeySerializer(keyPrefix));
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 创建使用系统键前缀的声明式缓存管理器。
     *
     * @param connectionFactory Redis 连接工厂
     * @param keyPrefix 系统级 Redis 键前缀
     * @return Redis 缓存管理器
     */
    @Bean
    public CacheManager cacheManager(
            RedisConnectionFactory connectionFactory,
            @Value("${system.redis.key-prefix:games-platform}") String keyPrefix) {
        String systemPrefix = normalizeKeyPrefix(keyPrefix);
        RedisCacheConfiguration cacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(cacheName -> systemPrefix + cacheName + ":")
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .disableCachingNullValues();
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(cacheConfiguration)
                .build();
    }

    /**
     * 规范化 Redis 系统键前缀。
     *
     * @param keyPrefix 原始系统键前缀
     * @return 空字符串或以冒号结尾的系统键前缀
     */
    private String normalizeKeyPrefix(String keyPrefix) {
        String normalized = keyPrefix == null ? "" : keyPrefix.trim();
        return normalized.isEmpty() || normalized.endsWith(":")
                ? normalized
                : normalized + ":";
    }
}
