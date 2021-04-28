package org.nutz.walnut.ext.sys.quota;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpInput;
import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.nutz.lang.Lang;
import org.nutz.web.handler.JettyHandlerCallback;

public class JettyMonitorHandler implements JettyHandlerCallback {

    protected QuotaService quotaService;

    public JettyMonitorHandler(QuotaService quotaService) {
        this.quotaService = quotaService;
    }

    @Override
    public void handle(Handler handler,
                       String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {
        try {
            handler.handle(target, baseRequest, request, response);
        }
        // 这玩意就别打印错误了
        catch (org.eclipse.jetty.io.EofException e) {}
        // 这玩意就别打印错误了
        catch (RuntimeException e) {
            Throwable e2 = Lang.unwrapThrow(e);
            if (e2 instanceof org.eclipse.jetty.io.EofException) {} else {
                throw e;
            }
        }
        // 记录流量
        finally {
            String wn_www_grp = (String) request.getAttribute("wn_www_grp");
            if (wn_www_grp != null) {
                HttpInput input = ((Request) request).getHttpInput();
                HttpOutput output = ((Response) response).getHttpOutput();
                long income = input.getContentConsumed();
                long outgo = output.getWritten();
                quotaService.incrUsage(wn_www_grp, "network", income + outgo);
            }
        }
    }

}
