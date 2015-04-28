package org.nutz.walnut.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.local.AllLocalTest;
import org.nutz.walnut.impl.mongo.AllMongoTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllLocalTest.class, AllMongoTest.class})
public class AllImplTest {}
