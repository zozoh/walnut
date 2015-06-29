package org.nutz.walnut.impl.hook;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.hook.local.LocalWnHookTest;
import org.nutz.walnut.impl.hook.mongo.MongoWnHookTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({LocalWnHookTest.class, MongoWnHookTest.class})
public class AllImplHookTest {}
