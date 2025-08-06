package com.site0.walnut.cache.temp;

import static org.junit.Assert.*;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WnInterimCacheTest {

    private WnInterimCache<String> cache;
    private static final int EXPIRE_SECONDS = 1; // 1秒过期
    private static final int CLEAN_THRESHOLD = 1; // 超过1个就清理

    @Before
    public void setUp() {
        cache = new WnInterimCache<>(EXPIRE_SECONDS, CLEAN_THRESHOLD);
    }

    // 基础功能测试
    @Test
    public void testBasicPutAndGet() {
        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));
    }

    // 获取不存在的键
    @Test
    public void testGetNonExistentKey() {
        assertNull(cache.get("non_existent"));
    }

    // 过期机制测试
    @Test
    public void testExpiration() throws InterruptedException {
        cache.put("key1", "value1");
        TimeUnit.MILLISECONDS.sleep(EXPIRE_SECONDS * 1000L + 100); // 稍微超过过期时间
        assertNull("缓存项应已过期", cache.get("key1"));
    }

    // 手动移除测试
    @Test
    public void testRemove() {
        cache.put("key1", "value1");
        cache.remove("key1");
        assertNull(cache.get("key1"));
    }

    @Test
    public void testRemoveNonExistentKey() {
        assertNull(cache.remove("nonexistent"));
    }

    // 清理过期项测试
    @Test
    public void testCleanUp() throws InterruptedException {
        cache.put("key1", "value1");
        TimeUnit.MILLISECONDS.sleep(EXPIRE_SECONDS * 1000 + 100);
        cache.cleanUp();
        assertEquals(0, cache.size());
    }

    // 自动清理触发测试
    @Test
    public void testAutoCleanUpTrigger() throws InterruptedException {
        // 填充超过100个缓存项
        for (int i = 0; i < 101; i++) {
            cache.put("key" + i, "value" + i);
        }
        TimeUnit.MILLISECONDS.sleep(EXPIRE_SECONDS * 1000 + 100);
        cache.get("key0"); // 触发自动清理
        assertTrue("缓存大小应小于101", cache.size() < 101);
    }

    // 更新缓存项测试
    @Test
    public void testUpdateItem() throws InterruptedException {
        cache.put("key1", "value1");
        TimeUnit.MILLISECONDS.sleep(500);
        cache.put("key1", "new_value"); // 更新值和过期时间

        // 验证更新后未过期
        assertEquals("new_value", cache.get("key1"));

        // 等待原始过期时间后验证
        TimeUnit.MILLISECONDS.sleep(600);
        assertNotNull("更新后应重置过期时间", cache.get("key1"));
    }

    // 清空缓存测试
    @Test
    public void testClearAll() {
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.clearAll();
        assertEquals(0, cache.size());
        assertNull(cache.get("key1"));
    }

    // 并发访问测试
    @Test
    public void testConcurrentAccess() throws InterruptedException {
        int threadCount = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final String key = "key" + i;
            executor.submit(() -> {
                cache.put(key, "value");
                assertNotNull(cache.get(key));
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();
        assertEquals(threadCount, cache.size());
    }

    // 过期项不计入大小测试
    @Test
    public void testSizeExcludesExpiredItems() throws InterruptedException {
        // 首次获取，记录了最后清理时间，之后获取就会触发清理了
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        TimeUnit.MILLISECONDS.sleep(EXPIRE_SECONDS * 1000 + 100);

        // 获取操作会触发清理
        cache.put("key3", "value3");

        assertEquals(1, cache.size());
    }
}