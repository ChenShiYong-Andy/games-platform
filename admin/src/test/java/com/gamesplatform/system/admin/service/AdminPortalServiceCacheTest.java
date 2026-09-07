package com.gamesplatform.system.admin.service;

import com.gamesplatform.school.english.dto.DailyEnglishConfigRequest;
import com.gamesplatform.system.admin.service.impl.AdminPortalServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.CacheEvict;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 管理后台每日英语缓存失效声明测试。
 */
class AdminPortalServiceCacheTest {

    /**
     * 验证每日英语配置更新后通过注解清空对应缓存空间。
     *
     * @throws NoSuchMethodException 方法不存在时抛出
     */
    @Test
    void updateEnglishConfigEvictsDailyEnglishCache() throws NoSuchMethodException {
        Method method = AdminPortalServiceImpl.class
                .getMethod("updateEnglishConfig", DailyEnglishConfigRequest.class);

        CacheEvict cacheEvict = method.getAnnotation(CacheEvict.class);

        assertArrayEquals(new String[]{"daily-english:practice"}, cacheEvict.cacheNames());
        assertTrue(cacheEvict.allEntries());
    }
}
