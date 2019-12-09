package org.nutz.walnut.util;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestCmds.class,
                     TestWn.class,
                     TestWnRg.class,
                     TestZParams.class,
                     TestJvmTunnel.class})
public class TestAllWnUtil {}
