package org.nutz.walnut.api.io;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.api.io.local.AllLocalTest;
import org.nutz.walnut.api.io.mongo.AllMongoTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllLocalTest.class, AllMongoTest.class, WnIoTest.class})
public class AllWnIoTest {}
