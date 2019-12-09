package org.nutz.walnut.impl;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.auth.TestAllAuth;
import org.nutz.walnut.impl.box.TestAllBox;
import org.nutz.walnut.impl.hook.TestIoHook;
import org.nutz.walnut.impl.io.TestAllImplIo;

@RunWith(Suite.class)
@Suite.SuiteClasses({TestAllImplIo.class,
                     TestAllAuth.class,
                     TestIoHook.class,
                     TestAllBox.class,})
public class TestAllImpl {}
