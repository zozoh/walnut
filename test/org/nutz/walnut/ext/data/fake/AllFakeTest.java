package org.nutz.walnut.ext.data.fake;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.ext.data.fake.impl.WnIntTmplFakerTest;
import org.nutz.walnut.ext.data.fake.impl.WnIntegerFakerTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({WnIntegerFakerTest.class, WnIntTmplFakerTest.class})
public class AllFakeTest {}
