package com.site0.walnut.web2;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * web2 包测试套件（RM-NUTZ-05 起）：聚合 web2 内的单元测试。
 * <p>
 * 后续 RM-NUTZ-06~09 的控制器/过滤器测试也加入本套件；由
 * {@code com.site0.walnut.AllWnBaseTest} 统一纳入全量回归。
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({Jetty05ChainTest.class, Jetty06SwitchSessionTest.class, Jetty07AppTest.class,
                     Jetty08ObjTest.class, Jetty09HttpApiTest.class})
public class AllWeb2Test {}
