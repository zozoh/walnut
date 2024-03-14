package com.site0.walnut.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.core.bean.WnIoObjTest;
import com.site0.walnut.core.bean.WnObjIdTest;
import com.site0.walnut.core.bm.AllBMTest;
import com.site0.walnut.core.indexer.AllIndexerTest;
import com.site0.walnut.core.mapping.AllIoMappingTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WnObjIdTest.class,
                     WnIoObjTest.class,
                     AllIndexerTest.class,
                     AllBMTest.class,
                     AllIoMappingTest.class})
public class AllCoreTest {}
