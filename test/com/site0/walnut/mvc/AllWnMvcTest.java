package com.site0.walnut.mvc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * JETTY-03 mvc 包测试套件：路由/参数绑定/视图/动作链/调度分流全矩阵。
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({WnMvcMappingTest.class,
                     WnMvcParamBindTest.class,
                     WnMvcViewTest.class,
                     WnMvcChainTest.class,
                     WnMvcDispatcherTest.class,})
public class AllWnMvcTest {}
