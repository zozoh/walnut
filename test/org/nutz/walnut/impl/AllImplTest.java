package org.nutz.walnut.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.auth.WnSysAuthServiceTest;
import org.nutz.walnut.impl.box.AllImplBoxTest;
import org.nutz.walnut.impl.hook.IoHookTest;
import org.nutz.walnut.impl.io.AllImplIoTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllImplIoTest.class,
                     WnSysAuthServiceTest.class,
                     IoHookTest.class,
                     AllImplBoxTest.class,})
public class AllImplTest {}
