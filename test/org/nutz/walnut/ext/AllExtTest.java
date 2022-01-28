package org.nutz.walnut.ext;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.ext.app.impl.AppInitServiceTest;
import org.nutz.walnut.ext.data.fake.AllFakeTest;
import org.nutz.walnut.ext.data.o.util.WnPopsTest;
import org.nutz.walnut.ext.dsync.bean.WnDataSyncItemTest;
import org.nutz.walnut.ext.httpapi.HttpApiDynamicRenderTest;
import org.nutz.walnut.ext.media.ooml.AllOomlTest;
import org.nutz.walnut.ext.mq.WnMqMessageTest;
import org.nutz.walnut.ext.net.AllNetTest;
import org.nutz.walnut.ext.sys.AllSysTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
                     /* AllBulkTest.class, */
                     AppInitServiceTest.class,
                     HttpApiDynamicRenderTest.class,
                     AllOomlTest.class,
                     AllFakeTest.class,
                     WnPopsTest.class,
                     AllNetTest.class,
                     AllSysTest.class,
                     WnDataSyncItemTest.class,
                     WnMqMessageTest.class})
public class AllExtTest {}
