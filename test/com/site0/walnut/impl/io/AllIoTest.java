package com.site0.walnut.impl.io;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({WnIoImpl2Test.class,
                     WnIoCustomizedPvgTest.class,
                     LocalMappingTest.class,
                     RedisBMMappingTest.class})
public class AllIoTest {}
