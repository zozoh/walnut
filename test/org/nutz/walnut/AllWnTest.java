package org.nutz.walnut;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.core.AllCoreTest;
import org.nutz.walnut.ext.AllExtTest;
import org.nutz.walnut.impl.AllImplTest;
import org.nutz.walnut.util.AllUtilTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllCoreTest.class, AllUtilTest.class, AllImplTest.class, AllExtTest.class})
public class AllWnTest {}
