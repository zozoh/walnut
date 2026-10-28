package com.site0.walnut.security.captcha.impl.store;

import org.junit.After;
import org.junit.Before;

import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.security.captcha.impl.tianai.WnRedisCaptchaTicketStore;

public class WnRedisCaptchaTicketStoreTest extends AbstractWnCaptchaTicketStoreTest {

    @Before
    public void setUp() throws Exception {
        WedisConfig conf = this.setup.getWedisConfig();
        this.store = new WnRedisCaptchaTicketStore(conf, "captcha:unit-test:", 600000);
        this.setup.cleanRedisData();
    }

    @After
    public void tearDown() throws Exception {}

}
