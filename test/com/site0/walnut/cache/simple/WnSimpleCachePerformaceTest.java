package com.site0.walnut.cache.simple;

import org.junit.Test;

import com.site0.walnut.cache.WnCache;

public class WnSimpleCachePerformaceTest {

    @Test
    public void testPerformance() {
        System.out.println("WnSimpleCachePerformaceTest:");
        WnCache<String> cache = new WnSimpleCache<>(1000, 2000, 30);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            cache.put("key" + i, "value" + i);
        }
        long end = System.currentTimeMillis();
        System.out.println("Time taken to put 100,000 items: " + (end - start) + "ms");

        start = System.currentTimeMillis();
        for (int i = 0; i < 100000; i++) {
            cache.get("key" + i);
        }
        end = System.currentTimeMillis();
        System.out.println("Time taken to get 100,000 items: " + (end - start) + "ms");
    }
}
