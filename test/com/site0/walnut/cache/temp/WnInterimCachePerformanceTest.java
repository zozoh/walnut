package com.site0.walnut.cache.temp;

import org.junit.Test;

import com.site0.walnut.cache.WnCache;

public class WnInterimCachePerformanceTest {

    @Test
    public void testPerformance() {
        System.out.println("WnInterimCachePerformanceTest:");
        WnCache<String> cache = new WnInterimCache<>(3, 1000);
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
