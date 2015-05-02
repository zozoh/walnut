package org.nutz.walnut.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.box.AllImplBoxTest;
import org.nutz.walnut.impl.io.AllImplIoTest;
import org.nutz.walnut.impl.usr.AllImplUsrTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllImplIoTest.class, AllImplUsrTest.class, AllImplBoxTest.class})
public class AllImplTest {}
