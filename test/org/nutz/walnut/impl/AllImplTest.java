package org.nutz.walnut.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.auth.AllAuthTest;
import org.nutz.walnut.impl.box.AllBoxTest;
import org.nutz.walnut.impl.hook.IoHookTest;
import org.nutz.walnut.impl.io.AllIoTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllIoTest.class,
                     AllAuthTest.class,
                     IoHookTest.class,
                     AllBoxTest.class,})
public class AllImplTest {}
