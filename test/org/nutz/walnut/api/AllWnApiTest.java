package org.nutz.walnut.api;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.api.usr.WnUsrTest;
import org.nutz.walnut.impl.AllImplTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllImplTest.class, WnUsrTest.class})
public class AllWnApiTest {}
