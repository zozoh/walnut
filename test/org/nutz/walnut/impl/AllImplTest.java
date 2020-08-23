package org.nutz.walnut.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.auth.AllAuthTest;
import org.nutz.walnut.impl.box.AllInBoxTest;
import org.nutz.walnut.impl.io.AllIoTest;
import org.nutz.walnut.impl.lock.AllLockTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllIoTest.class, AllLockTest.class, AllAuthTest.class, AllInBoxTest.class,})
public class AllImplTest {}
