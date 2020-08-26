package org.nutz.walnut.core.bm;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.core.bm.localbm.LocalIoBMTest;
import org.nutz.walnut.core.bm.localfile.LocalFileBMTest;
import org.nutz.walnut.core.bm.redis.RedisBMTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({LocalIoBMTest.class, LocalFileBMTest.class, RedisBMTest.class})
public class AllBMTest {}
