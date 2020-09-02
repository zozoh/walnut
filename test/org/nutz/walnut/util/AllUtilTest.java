package org.nutz.walnut.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.util.upload.AllUploadTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({CmdsTest.class,
                     AllUploadTest.class,
                     WnTest.class,
                     WnRgTest.class,
                     ZParamsTest.class,
                     JvmTunnelTest.class})
public class AllUtilTest {}
