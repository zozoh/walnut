package com.site0.walnut.web2.controller.httpapi;

import com.site0.walnut.mvc.AbstractWnController;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcView;

/**
 * JETTY-09：{@code /api/?/**} 动态入口薄控制器（对照旧 {@code HttpApiModule.do_api}，
 * 类级 {@code WnAsUsr("root")} 由路由 headFilter {@code AsUsrFilter("root")} 承担）。
 *
 * <p>薄适配：{@code ?}/{@code **} 路径绑定从 {@code ctx.pathArg(0/1)} 取；
 * 每请求 new {@link HttpApiEngine} 并用 WnRun 的 setter 装配（不进 holder、
 * 不注册 web2.service）；返回 {@code null}（旧 {@code @Ok("void")} 语义），
 * 响应由引擎直写，业务错误在引擎内 sendError。</p>
 */
public class HttpApiController extends AbstractWnController {

    @Override
    public WnMvcView handle(WnMvcContext ctx) {
        String usr = ctx.pathArg(0);
        String api = ctx.pathArg(1);
        HttpApiEngine engine = __engine(ctx);
        engine.doApi(usr, api, ctx.request(), ctx.response());
        return null;
    }

    /**
     * 装配引擎（JETTY-04 已补的 WnRun setter；集中在这一个私有方法里）。
     */
    private HttpApiEngine __engine(WnMvcContext ctx) {
        HttpApiEngine engine = new HttpApiEngine();
        engine.setServices(ctx.services());
        engine.setIo(io(ctx));
        engine.setAuth(auth(ctx));
        engine.setConf(conf(ctx));
        engine.setBoxes(boxes(ctx));
        engine.setHooks(hooks(ctx));
        engine.setJef(jef(ctx));
        engine.setAllocTimeout(conf(ctx).getInt("box-alloc-timeout", 0));
        return engine;
    }

}
