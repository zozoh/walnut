package org.nutz.walnut;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.api.io.AllWnIoTest;
import org.nutz.walnut.util.AllWnUtilTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllWnUtilTest.class, AllWnIoTest.class})
public class WnTestAll {}
