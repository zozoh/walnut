package com.site0.walnut.security.captcha.impl.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.site0.walnut.core.IoCoreTest;
import com.site0.walnut.security.captcha.WnCaptchaAnswerStore;

/**
 * 验证码答案存储的**测试基类，不要直接运行**。
 * <p>
 * 本类的 {@code @Test} 依赖 {@link #store}，由子类在 {@code @Before} 里注入；
 * 直接实例化会 NPE。声明为 {@code abstract} 是给 surefire 的信号——抽象类不参与扫描。
 * 具体实现见子类 {@code WnRedisCaptchaAnswerStoreTest}。
 */
public abstract class AbstractWnCaptchaAnswerStoreTest extends IoCoreTest {

    /** 由子类在 @Before 注入，本类不初始化 */
    protected WnCaptchaAnswerStore store;

    /**
     * 保存的答案应能原样取回。
     */
    @Test
    public void test_01() throws Exception {
        store.save("id-01", "answer-01", 60000);

        assertEquals("answer-01", store.get("id-01"));
    }

    /**
     * 同 id 重复保存，新答案覆盖旧答案。
     */
    @Test
    public void test_02() throws Exception {
        store.save("id-02", "answer-old", 60000);
        store.save("id-02", "answer-new", 60000);

        assertEquals("answer-new", store.get("id-02"));
    }

    /**
     * 删除后不应再取得到。
     */
    @Test
    public void test_03() throws Exception {
        store.save("id-03", "answer-03", 60000);
        store.remove("id-03");

        assertNull(store.get("id-03"));
    }

    /**
     * 未保存的答案取回应为 null。
     */
    @Test
    public void test_04() throws Exception {
        assertNull(store.get("not-exists"));
    }

    /**
     * 过期后答案作废。
     */
    @Test
    public void test_05() throws Exception {
        store.save("id-05", "answer-05", 300);
        Thread.sleep(800);

        assertNull(store.get("id-05"));
    }

}
