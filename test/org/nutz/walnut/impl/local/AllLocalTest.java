package org.nutz.walnut.impl.local;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.local.data.LocalDataWnStoreTest;
import org.nutz.walnut.impl.local.sha1.LocalSha1WnStoreTest;
import org.nutz.walnut.impl.local.tree.LocalTreeWnStoreTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({MemNodeMapTest.class,
                     LocalWnTreeTest.class,
                     LocalDataWnStoreTest.class,
                     LocalTreeWnStoreTest.class,
                     LocalSha1WnStoreTest.class,
                     LocalWnIoTest.class})
public class AllLocalTest {}
