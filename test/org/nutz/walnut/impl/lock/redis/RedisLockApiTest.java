package org.nutz.walnut.impl.lock.redis;

import org.junit.After;
import org.junit.Before;
import org.nutz.walnut.api.lock.AbstractWnLockApiTest;

public class RedisLockApiTest extends AbstractWnLockApiTest {

    @Before
    public void setUp() throws Exception {
        this.locks = this.setup.getRedisLockApi(3);
        this.setup.cleanRedisData();
    }

    @After
    public void tearDown() throws Exception {}

}
