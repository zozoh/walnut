package org.nutz.walnut.api.io.local;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.api.io.local.data.LocalDataWnStoreTest;
import org.nutz.walnut.api.io.local.sha1.LocalSha1WnStoreTest;
import org.nutz.walnut.api.io.local.tree.LocalTreeWnStoreTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({MemNodeMapTest.class,
                     LocalWnTreeTest.class,
                     LocalDataWnStoreTest.class,
                     LocalTreeWnStoreTest.class,
                     LocalSha1WnStoreTest.class})
public class AllLocalTest {}
