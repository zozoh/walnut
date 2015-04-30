package org.nutz.walnut.impl.io;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.io.local.AllLocalIoTest;
import org.nutz.walnut.impl.io.mongo.AllMongoIoTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllLocalIoTest.class, AllMongoIoTest.class})
public class AllImplIoTest {}
