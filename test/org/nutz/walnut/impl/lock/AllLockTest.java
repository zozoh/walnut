package org.nutz.walnut.impl.lock;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.lock.redis.RedisLockApiTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({RedisLockApiTest.class})
public class AllLockTest {}
