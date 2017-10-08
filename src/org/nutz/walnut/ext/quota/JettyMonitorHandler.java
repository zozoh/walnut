package org.nutz.walnut.ext.quota;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpInput;
import org.eclipse.jetty.server.HttpOutput;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
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
        } finally {
            String wn_www_host = (String) request.getAttribute("wn_www_host");
            if (wn_www_host != null) {
                HttpInput input = ((Request)request).getHttpInput();
                HttpOutput output = ((Response)response).getHttpOutput();
                long income = input.getContentConsumed();
                long outgo = output.getWritten();
                quotaService.incrNetworkUsage(wn_www_host, income, outgo);
            }
        }
    }

    

}
