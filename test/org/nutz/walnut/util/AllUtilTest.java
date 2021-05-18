package org.nutz.walnut.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.util.time.WnDayTimeTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WsTest.class,
                     CmdsTest.class,
                     WnDayTimeTest.class,
                     WtimeTest.class,
                     WcharTest.class,
                     WnTest.class,
                     WnSortTest.class,
                     WregionTest.class,
                     ZParamsTest.class,
                     JvmTunnelTest.class})
public class AllUtilTest {}
