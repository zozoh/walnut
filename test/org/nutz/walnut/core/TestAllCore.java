package org.nutz.walnut.core;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.core.bean.WnObjIdTest;
import org.nutz.walnut.core.indexer.TestAllIndexer;
import org.nutz.walnut.core.io.TestAllIo;
import org.nutz.walnut.core.mapping.TestAllIoMapping;

@RunWith(Suite.class)
@Suite.SuiteClasses({WnObjIdTest.class,
                     TestAllIndexer.class,
                     TestAllIo.class,
                     TestAllIoMapping.class})
public class TestAllCore {}
