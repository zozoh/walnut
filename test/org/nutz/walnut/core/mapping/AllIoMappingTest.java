package org.nutz.walnut.core.mapping;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({MountInfoTest.class,
                     LocalFileIoMappingTest.class,
                     GlobalIoMappingTest.class,
                     GlobalRedisBMTest.class})
public class AllIoMappingTest {}
