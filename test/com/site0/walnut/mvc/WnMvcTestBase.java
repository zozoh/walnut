package com.site0.walnut.mvc;

import org.junit.Before;

import com.site0.walnut.mvc.fake.FakeController;

/**
 * RM-NUTZ-03 测试基座：假 controller 状态复位 + 常用装配/断言助手。
 *
 * <p>用例 ID 约定：T-01~T-19 路由/方法（{@link WnMvcMappingTest}）、
 * T-20~T-39 参数绑定（{@link WnMvcParamBindTest}）、T-40~T-59 视图/错误
 * （{@link WnMvcViewTest}）、T-60~T-69 动作链（{@link WnMvcChainTest}）、
 * T-70~T-89 调度与未命中回退（{@link WnMvcDispatcherTest}）。</p>
 */
public abstract class WnMvcTestBase {

    protected static final WnMvcFilter[] NO_FILTERS = new WnMvcFilter[0];

    @Before
    public void resetFakes() {
        FakeController.reset();
    }

    // ----------------------------------------------------------------------
    // 装配助手
    // ----------------------------------------------------------------------

    /** 新工厂（无服务；机制测试不需要 services/io/mimes）。 */
    protected WnMvcFactory newFactory() {
        return new WnMvcFactory();
    }

    /** 便捷注册：单方法 + 无过滤器。 */
    protected WnMvcMapping route(WnMvcMapping m,
                                 WnMvcHttpMethod method,
                                 String pattern,
                                 Class<? extends WnMvcController> ctl) {
        return m.route(new WnMvcHttpMethod[]{method}, pattern, ctl, NO_FILTERS, NO_FILTERS);
    }

    /** 便捷注册：ANY 方法 + 无过滤器。 */
    protected WnMvcMapping routeAny(WnMvcMapping m,
                                    String pattern,
                                    Class<? extends WnMvcController> ctl) {
        return m.route(new WnMvcHttpMethod[]{WnMvcHttpMethod.ANY},
                       pattern,
                       ctl,
                       NO_FILTERS,
                       NO_FILTERS);
    }

    /**
     * 走一次完整调度，返回捕获了响应的假响应对象。
     */
    protected MockWnMvcResponse dispatch(WnMvcDispatcher dispatcher,
                                         WnMvcFactory factory,
                                         MockWnMvcRequest req) {
        MockWnMvcResponse resp = new MockWnMvcResponse();
        WnMvcContext ctx = factory.newContext(req, resp);
        dispatcher.dispatch(ctx);
        return resp;
    }

    /**
     * 用「mapping + 空全局过滤器」构造调度器并走一次调度。
     */
    protected MockWnMvcResponse dispatch(WnMvcMapping mapping, MockWnMvcRequest req) {
        WnMvcFactory factory = newFactory();
        WnMvcDispatcher dispatcher = new WnMvcDispatcher(mapping,
                                                         factory,
                                                         NO_FILTERS,
                                                         NO_FILTERS);
        return dispatch(dispatcher, factory, req);
    }

}
