package org.nutz.walnut.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.util.obj.WnObjJoinFieldsTest;
import org.nutz.walnut.util.time.WnDayTimeTest;
import org.nutz.walnut.util.tmpl.AllTmplTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WsTest.class,
                     WnObjJoinFieldsTest.class,
                     AllTmplTest.class,
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
