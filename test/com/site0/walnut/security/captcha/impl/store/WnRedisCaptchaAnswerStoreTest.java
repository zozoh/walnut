package com.site0.walnut.security.captcha.impl.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.security.captcha.impl.tianai.WnRedisCaptchaAnswerStore;

import cloud.tianai.captcha.common.AnyMap;

public class WnRedisCaptchaAnswerStoreTest extends AbstractWnCaptchaAnswerStoreTest {

    private WnRedisCaptchaAnswerStore redisStore;

    @Before
    public void setUp() throws Exception {
        WedisConfig conf = this.setup.getWedisConfig();
        this.redisStore = new WnRedisCaptchaAnswerStore(conf, "captcha:unit-test:");
        this.store = redisStore;
        this.setup.cleanRedisData();
    }

    @After
    public void tearDown() throws Exception {}

    /**
     * 引擎侧 CacheStore 的存取：setCache 存、getCache 取。
     */
    @Test
    public void test_cache_01() throws Exception {
        AnyMap data = AnyMap.create();
        data.put("answer", "cache-answer");
        redisStore.setCache("captcha:unit-test:cache-01", data, 60000L, TimeUnit.MILLISECONDS);

        AnyMap re = redisStore.getCache("captcha:unit-test:cache-01");
        assertEquals("cache-answer", re.getString("answer"));
    }

    /**
     * getAndRemoveCache 取出后即删除（一发一验）。
     */
    @Test
    public void test_cache_02() throws Exception {
        AnyMap data = AnyMap.create();
        data.put("answer", "cache-answer");
        redisStore.setCache("captcha:unit-test:cache-02", data, 60000L, TimeUnit.MILLISECONDS);

        AnyMap re = redisStore.getAndRemoveCache("captcha:unit-test:cache-02");
        assertEquals("cache-answer", re.getString("answer"));

        // 已经删了，再取应为 null
        assertNull(redisStore.getCache("captcha:unit-test:cache-02"));
    }

    /**
     * incr 计数与读取。
     */
    @Test
    public void test_cache_03() throws Exception {
        Long re = redisStore.incr("captcha:unit-test:cache-03", 1, 60000L, TimeUnit.MILLISECONDS);
        assertEquals(Long.valueOf(1), re);

        re = redisStore.incr("captcha:unit-test:cache-03", 2, 60000L, TimeUnit.MILLISECONDS);
        assertEquals(Long.valueOf(3), re);

        assertEquals(Long.valueOf(3), redisStore.getLong("captcha:unit-test:cache-03"));
    }

    /**
     * 不存在的键，getCache / getLong 返回空。
     */
    @Test
    public void test_cache_04() throws Exception {
        assertNull(redisStore.getCache("captcha:unit-test:not-exists"));
        assertNull(redisStore.getLong("captcha:unit-test:not-exists"));
    }

    /**
     * 业务侧的存取与引擎侧的键空间互不干扰（ans: 前缀区隔）。
     */
    @Test
    public void test_cache_05() throws Exception {
        store.save("cache-05", "ans-answer", 60000);

        // Walnut 侧能取到
        assertEquals("ans-answer", store.get("cache-05"));
        // 引擎侧按 ans: 键能取到
        AnyMap re = redisStore.getCache("captcha:unit-test:ans:cache-05");
        assertEquals("ans-answer", re.getString("answer"));
        // 不带 ans: 前缀的键不受影响
        assertNull(redisStore.getCache("captcha:unit-test:cache-05"));
    }

    /**
     * CacheStore 的过期同样生效。
     */
    @Test
    public void test_cache_06() throws Exception {
        AnyMap data = AnyMap.create();
        data.put("answer", "cache-answer");
        assertTrue(redisStore.setCache("captcha:unit-test:cache-06", data, 300L, TimeUnit.MILLISECONDS));
        Thread.sleep(800);

        assertNull(redisStore.getCache("captcha:unit-test:cache-06"));
    }

}
