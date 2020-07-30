package org.nutz.walnut.ext;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.ext.httpapi.HttpApiDynamicRenderTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
                     /* AllBulkTest.class, */
                     HttpApiDynamicRenderTest.class})
public class AllExtTest {}
