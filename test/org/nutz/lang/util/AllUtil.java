package org.nutz.lang.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({LinkedArrayTest.class,
                     LinkedByteBufferTest.class,
                     LinkedCharArrayTest.class,
                     LinkedIntArrayTest.class,
                     IntRangeTest.class,
                     FloatRangeTest.class,
                     IntSetTest.class,
                     FloatSetTest.class,
                     SimpleNodeTest.class,
                     DisksTest.class,
                     ContextTest.class,
                     NutMapTest.class,
                     RegionTest.class,
                     MultiLinePropertiesTest.class,
                     ResidentStatusTest.class})
public class AllUtil {}
