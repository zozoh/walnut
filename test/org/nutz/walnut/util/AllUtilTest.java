package org.nutz.walnut.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.util.upload.AllUploadTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WsTest.class,
                     CmdsTest.class,
                     AllUploadTest.class,
                     WnTest.class,
                     WnSortTest.class,
                     WnRgTest.class,
                     ZParamsTest.class,
                     JvmTunnelTest.class})
public class AllUtilTest {}
