package org.nutz.walnut.api.io.local;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.api.io.local.sha1.LocalSha1WnStoreTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({MemNodeMapTest.class, LocalWnTreeTest.class, LocalSha1WnStoreTest.class})
public class AllLocalTest {}
