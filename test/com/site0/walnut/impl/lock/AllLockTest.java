package com.site0.walnut.impl.lock;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.impl.lock.redis.RedisLockApiTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WnLockObjTest.class, RedisLockApiTest.class})
public class AllLockTest {}
