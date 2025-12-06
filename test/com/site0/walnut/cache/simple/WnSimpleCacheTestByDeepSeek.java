package com.site0.walnut.cache.simple;

import static org.junit.Assert.*;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

public class WnSimpleCacheTestByDeepSeek {

    private static final int MIN_ITEMS = 3;
    private static final int MAX_ITEMS = 5;
    private static final int ITEM_DURATION = 1; // sec

    private WnSimpleCache<String> cache;

    @Before
    public void setUp() {
        cache = new WnSimpleCache<String>(MIN_ITEMS, MAX_ITEMS, ITEM_DURATION) {
            @Override
            public String loadItemData(String key) {
                return key.startsWith("loadable_") ? key.replace("loadable_", "") : null;
            }
        };
    }

    @Test
    public void test_add_and_get_item() {
        // 添加并获取项目
        cache.put("key1", "value1");
        assertEquals("value1", cache.get("key1"));

        // 验证缓存大小
        assertEquals(1, cache.size());
    }

    @Test
    public void test_get_loads_missing_item() {
        // 测试通过 loadItemData 加载项目
        assertEquals("test", cache.get("loadable_test"));
        assertEquals(1, cache.size());

        // 测试不存在的键
        assertNull(cache.get("invalid_key"));
    }

    @Test
    public void test_weight_increase_and_reordering() {
        // 添加三个项目
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        // 访问 key2 两次，key1 一次
        cache.get("key2");
        cache.get("key2");
        cache.get("key1");

        // 验证链表顺序 (权重: key2 > key1 > key3)
        assertEquals("key2", cache.head.getKey());
        assertEquals("key3", cache.tail.getKey());
        assertEquals("key1", cache.head.next().getKey());
    }

    @Test
    public void test_expiration_cleanup() throws Exception {
        // 添加会过期的项目
        cache.put("expired", "value");
        TimeUnit.MILLISECONDS.sleep(ITEM_DURATION * 1000L + 10); // 等待过期

        // 清理项目
        cache.cleanUp();

        // 验证已清理
        assertEquals(0, cache.size());
        assertNull(cache.head);
        assertNull(cache.tail);
    }

    @Test
    public void test_size_based_eviction() {
        // 添加超过最大限制的项目
        for (int i = 1; i <= MAX_ITEMS + 2; i++) {
            cache.put("key" + i, "value" + i);
        }

        // 触发清理 (应该清理到 MIN_ITEMS)
        cache.cleanUp();

        // 验证缓存大小
        assertEquals(MIN_ITEMS, cache.size());

        // 验证最低权重的项目被移除
        assertNull(cache.get("key1"));
        assertNull(cache.get("key2"));
    }

    @Test
    public void test_remove_item() {
        // 添加项目
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // 移除项目
        cache.remove("key1");

        // 验证移除
        assertEquals(1, cache.size());
        assertNull(cache.get("key1"));
        assertEquals("key2", cache.head.getKey());
        assertEquals("key2", cache.tail.getKey());
    }

    @Test
    public void test_clear_all() {
        // 添加项目
        cache.put("key1", "value1");
        cache.put("key2", "value2");

        // 清空缓存
        cache.clearAll();

        // 验证清空
        assertEquals(0, cache.size());
        assertNull(cache.head);
        assertNull(cache.tail);
    }

    @Test
    public void test_edge_cases() {
        // 测试空缓存操作
        cache.cleanUp();
        cache.clearAll();
        assertEquals(0, cache.size());

        // 测试添加空键
        cache.put(null, "value");
        assertEquals(0, cache.size());

        // 测试移除不存在的键
        assertNull(cache.remove("invalid_key"));

    }

    @Test
    public void test_linked_list_integrity() {
        // 添加三个项目
        cache.put("A", "value1");
        cache.put("B", "value2");
        cache.put("C", "value3");

        // 验证链表完整性
        assertEquals("C>B>A", cache.head.toNextChainAsKeyStr());
        assertEquals("A<B<C", cache.tail.toPrevChainAsKeyStr());

        // 验证双向链接
        assertNull(cache.head.prev());
        assertNull(cache.tail.next());
        assertEquals(cache.head, cache.head.next().prev());
    }

    @Test
    public void test_item_reordering_on_access() {
        // 添加项目
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.put("key3", "value3");

        // 访问最后一个项目使其移动到头部
        cache.get("key3");

        // 验证新顺序: key3 (权重1) > key1/key2 (权重0)
        assertEquals("key3", cache.head.getKey());
        assertTrue(cache.head.getWeight() > cache.head.next().getWeight());
    }
}