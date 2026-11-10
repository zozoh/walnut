package com.site0.walnut.web2.jetty;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import com.site0.walnut.log.WnLog;
import com.site0.walnut.mvc.WnMvcContext;
import com.site0.walnut.mvc.WnMvcDispatcher;
import com.site0.walnut.mvc.WnMvcFactory;
import com.site0.walnut.mvc.WnMvcFilter;
import com.site0.walnut.mvc.WnMvcFilterException;
import com.site0.walnut.mvc.WnMvcRequest;
import com.site0.walnut.mvc.WnMvcResponse;
import com.site0.walnut.util.Wlog;

/**
 * 把 Jetty12 core 请求接到 WnMvc（RM-NUTZ-14）：bornRequest/bornResponse → newContext
 * → 预分发过滤器 → {@link WnMvcDispatcher#dispatch(WnMvcContext)}。
 * <p>
 * 直接实现 core {@link Handler}（{@code handle(Request, Response, Callback)}），
 * 不经过 javax/jakarta servlet API。
 * <p>
 * 预分发过滤器只用于「必须在路由匹配前执行」的逻辑（Host→域 路径重写）；它同时也在
 * dispatcher 的 globalHead 中，链内以幂等标记跳过。
 * <p>
 * CORS/OPTIONS 统一由 globalHead 里的 CorsFilter 处理（RM-NUTZ-17），本 Handler 不特判。
 */
public class WnMvcJetty12Handler extends Handler.Abstract {

    private static final WnLog log = Wlog.getMAIN();

    private final WnMvcFactory factory;

    private final WnMvcDispatcher dispatcher;

    private final WnMvcFilter preFilter;

    public WnMvcJetty12Handler(WnMvcFactory factory,
                               WnMvcDispatcher dispatcher,
                               WnMvcFilter preFilter) {
        this.factory = factory;
        this.dispatcher = dispatcher;
        this.preFilter = preFilter;
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback)
            throws Exception {
        WnMvcRequest req = factory.bornRequest(request);
        WnMvcResponse resp = factory.bornResponse(response);
        WnMvcContext ctx = factory.newContext(req, resp);
        try {
            if (null != preFilter)
                preFilter.doFilter(ctx);
            dispatcher.dispatch(ctx);
        }
        catch (WnMvcFilterException fe) {
            if (!resp.committed())
                resp.sendError(fe.getStatus(), fe.getMessage());
        }
        catch (Throwable e) {
            if (!resp.committed()) {
                log.warn("WnMvcJetty12Handler: request failed @"
                         + req.getPath(),
                         e);
                resp.sendError(500);
            }
        }
        callback.succeeded();
        return true;
    }

}
