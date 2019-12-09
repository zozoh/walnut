package org.nutz.walnut;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.ext.AllExt;
import org.nutz.walnut.impl.TestAllImpl;
import org.nutz.walnut.util.TestAllWnUtil;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestAllWnUtil.class, TestAllImpl.class, AllExt.class})
public class WnTestAll {}
