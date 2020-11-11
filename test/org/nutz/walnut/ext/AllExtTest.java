package org.nutz.walnut.ext;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.ext.app.impl.AppInitServiceTest;
import org.nutz.walnut.ext.httpapi.HttpApiDynamicRenderTest;
import org.nutz.walnut.ext.mq.WnMqMessageTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
                     /* AllBulkTest.class, */
                     AppInitServiceTest.class,
                     HttpApiDynamicRenderTest.class,
                     WnMqMessageTest.class})
public class AllExtTest {}
