package org.nutz.walnut;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.core.AllCoreTest;
import org.nutz.walnut.core.eot.AllExpiObjTableTest;
import org.nutz.walnut.ext.AllExtTest;
import org.nutz.walnut.impl.AllImplTest;
import org.nutz.walnut.util.AllUtilTest;
import org.nutz.walnut.validate.WnMatchTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllCoreTest.class,
                     AllExpiObjTableTest.class,
                     AllUtilTest.class,
                     WnMatchTest.class,
                     AllImplTest.class,
                     AllExtTest.class})
public class AllWnBaseTest {}
