package com.site0.walnut;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.alg.AllAlgTest;
import com.site0.walnut.api.AllApiTest;
import com.site0.walnut.cheap.AllCheapTest;
import com.site0.walnut.core.AllCoreTest;
import com.site0.walnut.core.eot.AllExpiObjTableTest;
import com.site0.walnut.cron.WnCronTest;
import com.site0.walnut.ext.AllExtTest;
import com.site0.walnut.impl.AllImplTest;
import com.site0.walnut.ooml.OomlsTest;
import com.site0.walnut.util.AllUtilTest;
import com.site0.walnut.validate.WnMatchTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllAlgTest.class,
                     AllApiTest.class,
                     AllCheapTest.class,
                     AllCoreTest.class,
                     AllExpiObjTableTest.class,
                     AllUtilTest.class,
                     WnCronTest.class,
                     WnMatchTest.class,
                     OomlsTest.class,
                     AllImplTest.class,
                     AllExtTest.class})
public class AllWnBaseTest {}
