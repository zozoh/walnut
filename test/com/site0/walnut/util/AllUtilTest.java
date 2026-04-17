package com.site0.walnut.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import com.site0.walnut.util.bank.WbankTest;
import com.site0.walnut.util.explain.WnExplainsTest;
import com.site0.walnut.util.obj.WnObjJoinFieldsTest;
import com.site0.walnut.util.stream.MarkableInputStreamTest;
import com.site0.walnut.util.time.WnDayTimeTest;
import com.site0.walnut.util.tmpl.AllTmplTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WsTest.class,
                     WnObjJoinFieldsTest.class,
                     MarkableInputStreamTest.class,
                     AllTmplTest.class,
                     CmdsTest.class,
                     WnExplainsTest.class,
                     WnDayTimeTest.class,
                     WtimeTest.class,
                     WcharTest.class,
                     WnTest.class,
                     WnumTest.class,
                     WnSortTest.class,
                     WregionTest.class,
                     ZParamsTest.class,
                     JvmTunnelTest.class,
                     WbankTest.class})
public class AllUtilTest {}
