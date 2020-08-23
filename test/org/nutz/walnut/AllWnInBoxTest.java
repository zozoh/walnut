package org.nutz.walnut;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.nutz.walnut.impl.box.JvmBoxTest;
import org.nutz.walnut.impl.hook.IoHookTest;

/**
 * 不知道为啥，总是有失败，可能是流没有刷新，但是我又没找到哪里没刷新，靠烦恼啊！！！
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({JvmBoxTest.class, IoHookTest.class,})
public class AllWnInBoxTest {}
