package org.nutz.walnut;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.alg.AllAlgTest;
import org.nutz.walnut.api.AllApiTest;
import org.nutz.walnut.cheap.AllCheapTest;
import org.nutz.walnut.core.AllCoreTest;
import org.nutz.walnut.core.eot.AllExpiObjTableTest;
import org.nutz.walnut.cron.WnCronTest;
import org.nutz.walnut.ext.AllExtTest;
import org.nutz.walnut.impl.AllImplTest;
import org.nutz.walnut.util.AllUtilTest;
import org.nutz.walnut.validate.WnMatchTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllAlgTest.class,
                     AllApiTest.class,
                     AllCheapTest.class,
                     AllCoreTest.class,
                     AllExpiObjTableTest.class,
                     AllUtilTest.class,
                     WnCronTest.class,
                     WnMatchTest.class,
                     AllImplTest.class,
                     AllExtTest.class})
public class AllWnBaseTest {}
