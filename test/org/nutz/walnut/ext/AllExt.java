package org.nutz.walnut.ext;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.ext.bulk.AllBulkTest;
import org.nutz.walnut.ext.httpapi.TestHttpApiDynamicRender;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllBulkTest.class, TestHttpApiDynamicRender.class})
public class AllExt {}
