package com.site0.walnut.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.impl.auth.AllAuthTest;
import com.site0.walnut.impl.box.AllInBoxTest;
import com.site0.walnut.impl.io.AllIoTest;
import com.site0.walnut.impl.lock.AllLockTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllIoTest.class, AllLockTest.class, AllAuthTest.class, AllInBoxTest.class,})
public class AllImplTest {}
