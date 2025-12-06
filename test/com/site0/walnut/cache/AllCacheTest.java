package com.site0.walnut.cache;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.site0.walnut.cache.simple.WnSimpleCacheItemTest;
import com.site0.walnut.cache.simple.WnSimpleCachePerformaceTest;
import com.site0.walnut.cache.simple.WnSimpleCacheTestByDeepSeek;
import com.site0.walnut.cache.simple.WnSimpleCacheTestByDoubao;
import com.site0.walnut.cache.simple.WnSimpleCacheTestByGrok3;
import com.site0.walnut.cache.simple.WnSimpleCacheTestByKimi2;
import com.site0.walnut.cache.temp.WnInterimCachePerformanceTest;
import com.site0.walnut.cache.temp.WnInterimCacheTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WnSimpleCacheItemTest.class,
                     WnSimpleCacheTestByDoubao.class,
                     WnSimpleCacheTestByDeepSeek.class,
                     WnSimpleCacheTestByKimi2.class,
                     WnSimpleCacheTestByGrok3.class,
                     WnSimpleCachePerformaceTest.class,
                     WnInterimCacheTest.class,
                     WnInterimCachePerformanceTest.class})
public class AllCacheTest {}
