package com.site0.walnut.cache.simple;

import static org.junit.Assert.*;
import org.junit.*;

public class WnSimpleCacheTestByKimi2 {

    private StringCache cache;

    class StringCache extends WnSimpleCache<String> {

        boolean autoLoad;

        public StringCache(int min, int max, int du) {
            super(min, max, du);
            this.autoLoad = false;
        }

        @Override
        public String loadItemData(String key) {
            if (!this.autoLoad) {
                return null;
            }
            return "loaded:" + key;
        }
    }

    @Before
    public void setup() {
        cache = new StringCache(2, 5, 1);
    }

    @After
    public void tearDown() {
        cache.clearAll();
    }

    // ✅ 测试基本添加与获取
    @Test
    public void testAddAndGet() {
        cache.put("k1", "v1");
        assertEquals("v1", cache.get("k1"));
        assertEquals(1, cache.size());
    }

    // ✅ 测试重复添加覆盖
    @Test
    public void testAddDuplicateKey() {
        cache.put("k1", "v1");
        cache.put("k1", "v1-updated");
        assertEquals("v1-updated", cache.get("k1"));
        assertEquals(1, cache.size());
    }

    // ✅ 测试自动加载
    @Test
    public void testAutoLoad() {
        cache.autoLoad = true;
        assertEquals("loaded:k2", cache.get("k2"));
        assertEquals(1, cache.size());
    }

    // ✅ 测试权重更新与链表顺序
    @Test
    public void testWeightAndOrder() {
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.put("k3", "v3");

        // 初始顺序：k1<k2<k3 (head=k3, tail=k1)
        assertEquals("k3>k2>k1", cache.head.toNextChainAsKeyStr());

        // 访问 k2 两次
        cache.get("k2");
        cache.get("k2");

        // 现在 k2 权重最高
        assertEquals("k2>k3>k1", cache.head.toNextChainAsKeyStr());
    }

    // ✅ 测试移除
    @Test
    public void testRemove() {
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        assertEquals("v1", cache.remove("k1"));
        assertNull(cache.get("k1"));
        assertEquals(1, cache.size());
    }

    // ✅ 测试过期清理
    @Test
    public void testExpireCleanup() throws InterruptedException {
        cache = new StringCache(2, 5, 1); // 1000ms TTL
        cache.autoLoad = false;
        cache.put("k1", "v1");
        assertNotNull(cache.get("k1"));
        Thread.sleep(1200L);
        cache.cleanUp();
        assertNull(cache.get("k1"));
    }

    // ✅ 测试容量控制：超过 maxItemCount
    @Test
    public void testMaxCapacityCleanup() {
        cache = new StringCache(2, 3, 1000);
        cache.autoLoad = false;
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.put("k3", "v3");
        cache.put("k4", "v4");
        assertEquals(3, cache.size());
        assertNull(cache.get("k1")); // 最早插入的被淘汰
    }

    // ✅ 测试清理后保留 minItemCount
    @Test
    public void testMinCapacityRetention() {
        cache = new StringCache(2, 5, 10);
        for (int i = 1; i <= 5; i++) {
            cache.put("k" + i, "v" + i);
        }
        cache.cleanUp();
        assertEquals(2, cache.size());
    }

    // ✅ 测试空缓存链表操作
    @Test
    public void testEmptyCacheOperations() {
        assertNull(cache.head);
        assertNull(cache.tail);
        assertEquals(0, cache.size());
        cache.cleanUp(); // 不应抛异常
        cache.clearAll(); // 不应抛异常
    }

    // ✅ 测试单节点链表操作
    @Test
    public void testSingleNodeOperations() {
        cache.put("k1", "v1");
        assertEquals("k1", cache.head.toPrevChainAsKeyStr());
        assertEquals("k1", cache.tail.toNextChainAsKeyStr());

        cache.get("k1"); // 权重+1
        assertEquals("k1", cache.head.toPrevChainAsKeyStr());

        cache.remove("k1");
        assertNull(cache.head);
        assertNull(cache.tail);
    }

    // ✅ 测试双节点链表操作
    @Test
    public void testTwoNodeOperations() {
        cache.put("k1", "v1");
        cache.put("k2", "v2");

        assertEquals("k2>k1", cache.head.toNextChainAsKeyStr());
        assertEquals("k1<k2", cache.tail.toPrevChainAsKeyStr());

        cache.get("k1"); // k1 权重 > k2
        assertEquals("k1>k2", cache.head.toNextChainAsKeyStr());

        cache.remove("k1");
        assertEquals("k2", cache.head.toNextChainAsKeyStr());
        assertEquals("k2", cache.head.toPrevChainAsKeyStr());
        assertEquals(1, cache.size());
        assertTrue(cache.head == cache.tail);

        cache.remove("k2");
        assertNull(cache.head);
        assertNull(cache.tail);
        assertEquals(0, cache.size());
    }

    // ✅ 测试 clearAll
    @Test
    public void testClearAll() {
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.clearAll();
        assertEquals(0, cache.size());
        assertNull(cache.head);
        assertNull(cache.tail);
    }

    // ✅ 测试链表插入边界：插入到 head 前
    @Test
    public void testInsertBeforeHead() {
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.get("k2"); // k2 权重 > k1
        assertEquals("k2>k1", cache.head.toNextChainAsKeyStr());
    }

    // ✅ 测试链表插入边界：插入到 tail 后
    @Test
    public void testInsertAfterTail() {
        cache.put("k1", "v1");
        cache.put("k2", "v2");
        cache.put("k3", "v3");
        assertEquals("k3>k2>k1", cache.head.toNextChainAsKeyStr());
        assertEquals("k1<k2<k3", cache.tail.toPrevChainAsKeyStr());

    }

}
