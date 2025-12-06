package com.site0.walnut.ext.net.http;

import java.io.IOException;
import java.util.Map;

import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.http.bean.WnHttpResponse;
import com.site0.walnut.impl.box.JvmFilterContext;
import com.site0.walnut.util.Wlang;

public class HttpClientContext extends JvmFilterContext {

    public HttpContext context;

    public WnHttpResponse resp;

    public WnObj oOut;

    public HttpClientContext() {
        this.context = new HttpContext();
    }

    public boolean shouldOutputHeader() {
        return params.is("h") || params.is("H");
    }

    public boolean shouldOutputHeaderOnly() {
        return params.is("H");
    }

    public String outputHeader(WnHttpResponse resp) {
        StringBuilder sb = new StringBuilder();
        if (shouldOutputHeader()) {
            String line0 = String.format("%s/%s %s %s",
                                         resp.getProtocol(),
                                         resp.getVersion(),
                                         resp.getStatusCode(),
                                         resp.getStatusText());
            sb.append(line0);
            for (Map.Entry<String, Object> en : resp.getHeaders().entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                Wlang.each(val, (index, v, src) -> {
                    if (null == key) {
                        sb.append(v).append("\n");
                    } else {
                        sb.append(key).append(": ").append(v).append("\n");
                    }
                });
            }
        }
        return sb.toString();
    }

    public WnHttpResponse getRespose() throws IOException {
        if (null == resp) {
            HttpConnector c = context.open();
            c.prepare();
            c.connect();
            c.sendHeaders();
            c.sendBody();
            resp = c.getResponse();
        }
        return resp;
    }

    public String getErrReason() {
        return String.format("timeout-conn=%s, timeout-read=%s",
                             context.getConnectTimeout(),
                             context.getReadTimeout());
    }
}
