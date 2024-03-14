package com.site0.walnut.core.bm;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.core.bm.localbm.LocalIoBMTest;
import com.site0.walnut.core.bm.localfile.LocalFileBMTest;
import com.site0.walnut.core.bm.redis.RedisBMTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({LocalIoBMTest.class, LocalFileBMTest.class, RedisBMTest.class})
public class AllBMTest {}
