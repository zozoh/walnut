package com.site0.walnut.web2.jetty;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
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
 * 把 Servlet 请求接到 WnMvc：bornRequest/bornResponse → newContext → 预分发过滤器
 * → {@link WnMvcDispatcher#dispatch(WnMvcContext)}。
 * <p>
 * 预分发过滤器只用于「必须在路由匹配前执行」的逻辑（Host→域 路径重写）；它同时也在
 * dispatcher 的 globalHead 中，链内以幂等标记跳过。
 * <p>
 * CORS/OPTIONS 统一由 globalHead 里的 CorsFilter 处理（RM-NUTZ-16），本 Handler 不特判。
 */
public class WnMvcJettyHandler extends AbstractHandler {

    private static final WnLog log = Wlog.getMAIN();

    private final WnMvcFactory factory;

    private final WnMvcDispatcher dispatcher;

    private final WnMvcFilter preFilter;

    public WnMvcJettyHandler(WnMvcFactory factory,
                             WnMvcDispatcher dispatcher,
                             WnMvcFilter preFilter) {
        this.factory = factory;
        this.dispatcher = dispatcher;
        this.preFilter = preFilter;
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {
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
                log.warn("WnMvcJettyHandler: request failed @"
                         + req.getPath(),
                         e);
                resp.sendError(500);
            }
        }
        finally {
            baseRequest.setHandled(true);
        }
    }

}
