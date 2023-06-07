package org.nutz.walnut.alg;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.alg.ds.AllAlgDsTest;
import org.nutz.walnut.alg.stack.WnCharStackTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllAlgDsTest.class, WnCharStackTest.class})
public class AllAlgTest {}
