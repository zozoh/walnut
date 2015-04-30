package org.nutz.walnut.impl.usr;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.usr.local.LocalWnUsrTest;
import org.nutz.walnut.impl.usr.mongo.MongoWnUsrTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({LocalWnUsrTest.class, MongoWnUsrTest.class})
public class AllImplUsrTest {}
