package org.nutz.walnut.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.core.bean.WnIoObjTest;
import org.nutz.walnut.core.bean.WnObjIdTest;
import org.nutz.walnut.core.bm.AllBMTest;
import org.nutz.walnut.core.indexer.AllIndexerTest;
import org.nutz.walnut.core.mapping.AllIoMappingTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WnObjIdTest.class,
                     WnIoObjTest.class,
                     AllIndexerTest.class,
                     AllBMTest.class,
                     AllIoMappingTest.class})
public class AllCoreTest {}
