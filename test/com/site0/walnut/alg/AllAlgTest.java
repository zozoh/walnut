package com.site0.walnut.alg;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.site0.walnut.alg.ds.AllAlgDsTest;
import com.site0.walnut.alg.stack.WnCharStackTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({AllAlgDsTest.class, WnCharStackTest.class})
public class AllAlgTest {}
