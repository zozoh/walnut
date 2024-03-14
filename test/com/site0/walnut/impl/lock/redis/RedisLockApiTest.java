package com.site0.walnut.impl.lock.redis;

import org.junit.After;
import org.junit.Before;
import com.site0.walnut.api.lock.AbstractWnLockApiTest;

public class RedisLockApiTest extends AbstractWnLockApiTest {

    @Before
    public void setUp() throws Exception {
        this.locks = this.setup.getRedisLockApi(3);
        this.setup.cleanRedisData();
    }

    @After
    public void tearDown() throws Exception {}

}
