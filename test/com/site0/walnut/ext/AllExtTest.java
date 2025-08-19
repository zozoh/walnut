package com.site0.walnut.ext;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.ext.app.impl.AppInitServiceTest;
import com.site0.walnut.ext.data.fake.AllFakeTest;
import com.site0.walnut.ext.data.o.util.WnPopsTest;
import com.site0.walnut.ext.data.sqlx.AllSqlxTest;
import com.site0.walnut.ext.dsync.bean.WnDataSyncItemTest;
import com.site0.walnut.ext.httpapi.HttpApiDynamicRenderTest;
import com.site0.walnut.ext.media.AllMediasTest;
import com.site0.walnut.ext.mq.WnMqMessageTest;
import com.site0.walnut.ext.net.AllNetTest;
import com.site0.walnut.ext.sys.AllSysTest;
import com.site0.walnut.ext.xo.AllXoTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
                     /* AllBulkTest.class, */
                     AllXoTest.class,
                     AppInitServiceTest.class,
                     HttpApiDynamicRenderTest.class,
                     AllMediasTest.class,
                     AllFakeTest.class,
                     WnPopsTest.class,
                     AllNetTest.class,
                     AllSysTest.class,
                     WnDataSyncItemTest.class,
                     WnMqMessageTest.class,
                     AllSqlxTest.class})
public class AllExtTest {}
