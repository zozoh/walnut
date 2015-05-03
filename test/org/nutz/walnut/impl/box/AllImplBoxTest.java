package org.nutz.walnut.impl.box;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.box.local.LocalJvmBoxTest;
import org.nutz.walnut.impl.box.mongo.MongoJvmBoxTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({JvmsTest.class, JvmCmdTest.class, LocalJvmBoxTest.class, MongoJvmBoxTest.class})
public class AllImplBoxTest {}
