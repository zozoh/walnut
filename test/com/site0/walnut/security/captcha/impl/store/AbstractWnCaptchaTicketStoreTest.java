package com.site0.walnut.security.captcha.impl.store;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.site0.walnut.core.IoCoreTest;
import com.site0.walnut.security.captcha.WnCaptchaTicketStore;

public class AbstractWnCaptchaTicketStoreTest extends IoCoreTest {

    /**
     * 子类需要设置这个参数
     */
    protected WnCaptchaTicketStore store;

    /**
     * 签发的票据，用绑定的账号校验应通过。
     */
    @Test
    public void test_01() throws Exception {
        String ticket = store.createTicket("user@site0.xyz", 60000);
        assertNotNull(ticket);

        assertTrue(store.checkTicket(ticket, "user@site0.xyz"));
    }

    /**
     * 票据是一次性的：校验通过后立即作废。
     */
    @Test
    public void test_02() throws Exception {
        String ticket = store.createTicket("user@site0.xyz", 60000);
        assertTrue(store.checkTicket(ticket, "user@site0.xyz"));

        // 第二次校验应失败
        assertFalse(store.checkTicket(ticket, "user@site0.xyz"));
    }

    /**
     * 账号不匹配（重放别人的票据）不能通过。
     */
    @Test
    public void test_03() throws Exception {
        String ticket = store.createTicket("user@site0.xyz", 60000);
        assertFalse(store.checkTicket(ticket, "hacker@site0.xyz"));

        // 账号不匹配不消费票据，原账号仍能校验通过
        assertTrue(store.checkTicket(ticket, "user@site0.xyz"));
    }

    /**
     * 不存在的票据不能通过。
     */
    @Test
    public void test_04() throws Exception {
        assertFalse(store.checkTicket("not-exists-ticket", "user@site0.xyz"));
    }

    /**
     * 空票据或空账号直接拒绝。
     */
    @Test
    public void test_05() throws Exception {
        String ticket = store.createTicket("user@site0.xyz", 60000);

        assertFalse(store.checkTicket(null, "user@site0.xyz"));
        assertFalse(store.checkTicket(ticket, null));
        assertFalse(store.checkTicket("", "user@site0.xyz"));
    }

    /**
     * 过期后票据作废。
     */
    @Test
    public void test_06() throws Exception {
        String ticket = store.createTicket("user@site0.xyz", 300);
        Thread.sleep(800);

        assertFalse(store.checkTicket(ticket, "user@site0.xyz"));
    }

}
