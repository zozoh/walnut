package com.site0.walnut.security.otp.impl.store;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.site0.walnut.core.IoCoreTest;
import com.site0.walnut.security.otp.WnOTPBuilder;
import com.site0.walnut.security.otp.WnOTPStore;
import com.site0.walnut.security.otp.bean.WnOTPBean;
import com.site0.walnut.security.otp.bean.WnOTPType;

/**
 * OTP 存储的**测试基类，不要直接运行**。
 * <p>
 * 本类的 {@code @Test} 依赖 {@link #store} 与 {@link #builder}，由子类在 {@code @Before} 里注入；
 * 直接实例化会 NPE。声明为 {@code abstract} 是给 surefire 的信号——抽象类不参与扫描。
 * 具体实现见子类 {@code WnRedisOTPStoreTest}。
 */
public abstract class AbstractWnOTPStoreTest extends IoCoreTest {

    /** 由子类在 @Before 注入，本类不初始化 */
    protected WnOTPStore store;

    /** 由子类在 @Before 注入，本类不初始化 */
    protected WnOTPBuilder builder;

    /**
     * 保存的 OTP 应能用对应的类型、账号和值校验出来。
     */
    @Test
    public void test_01() throws Exception {
        WnOTPType type = WnOTPType.EMAIL;
        String account = "test@site0.xyz";

        WnOTPBean otp = builder.make(type, account);

        assertTrue(store.save(otp));
        assertEquals(otp, store.check(type, account, otp.getValue()));
    }

    /**
     * 保存的 OTP 使用错误的值校验时应返回空。
     */
    @Test
    public void test_02() throws Exception {
        WnOTPType type = WnOTPType.EMAIL;
        String account = "test@site0.xyz";
        WnOTPBean otp = builder.make(type, account);

        assertTrue(store.save(otp));
        assertNull(store.check(type, account, "wrong-value"));
    }

    /**
     * 删除已保存的 OTP 后，不应再能校验出来。
     */
    @Test
    public void test_03() throws Exception {
        WnOTPType type = WnOTPType.EMAIL;
        String account = "test@site0.xyz";
        WnOTPBean otp = builder.make(type, account);

        assertTrue(store.save(otp));
        assertTrue(store.remove(otp));
        assertFalse(store.remove(otp));

        assertNull(store.check(type, account, otp.getValue()));
    }

    /**
     * 未保存的 OTP 不应被校验出来。
     */
    @Test
    public void test_0004() throws Exception {
        WnOTPType type = WnOTPType.EMAIL;
        String account = "not-exists@site0.xyz";

        assertNull(store.check(type, account, "any-value"));
    }

    /**
     * 连续输错达到最大重试次数后，OTP 应与已删除的数据一样不可用。
     */
    @Test
    public void test_0005() throws Exception {
        WnOTPType type = WnOTPType.EMAIL;
        String account = "test@site0.xyz";
        WnOTPBean otp = builder.make(type, account);

        assertTrue(store.save(otp));
        assertNull(store.check(type, account, "wrong-value"));
        assertNull(store.check(type, account, "wrong-value"));
        assertNull(store.check(type, account, "wrong-value"));
        assertNull(store.check(type, account, "wrong-value"));

        assertNull(store.get(type, account));
        assertNull(store.check(type, account, otp.getValue()));
    }

}
