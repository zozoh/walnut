package com.site0.walnut.security.otp.impl.store;

import org.junit.After;
import org.junit.Before;
import com.site0.walnut.ext.sys.redis.WedisConfig;
import com.site0.walnut.security.otp.impl.builder.WnCharOTPBuilder;

public class WnRedisOTPStoreTest extends AbstractWnOTPStoreTest {

    @Before
    public void setUp() throws Exception {
        WedisConfig conf = this.setup.getWedisConfig();
        this.store = new WnRedisOTPStore(conf, "unit-test");
        this.builder = new WnCharOTPBuilder(4, 300);
        this.setup.cleanRedisData();
    }

    @After
    public void tearDown() throws Exception {}

}
