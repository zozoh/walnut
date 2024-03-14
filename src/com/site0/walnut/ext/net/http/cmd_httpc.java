package com.site0.walnut.ext.net.http;

import java.io.PrintStream;
import java.util.Map;

import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.http.bean.WnHttpResponse;
import com.site0.walnut.ext.net.util.WnNet;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wlang;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.obj.WnObjGetter;

public class cmd_httpc extends JvmFilterExecutor<HttpClientContext, HttpClientFilter> {

    public cmd_httpc() {
        super(HttpClientContext.class, HttpClientFilter.class);
    }

    @Override
    protected HttpClientContext newContext() {
        return new HttpClientContext();
    }

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "rhH");
    }

    @Override
    protected void prepare(WnSystem sys, HttpClientContext fc) {
        String url = fc.params.val_check(0);
        // 第一参数如果是个 HTTP 的方法，那么第二个参数就必然是 URL
        try {
            HttpMethod method = HttpMethod.valueOf(url.toUpperCase());
            if (null != method) {
                fc.context.setMethod(method);
                url = fc.params.val(1);
            }
        }
        catch (Throwable e) {}

        // 设置上下文
        fc.context.setUrl(url);
        fc.context.setFollowRedirects(fc.params.is("r"));
        fc.context.setInputStreamFactory(new WnObjGetter(sys));
    }

    @Override
    protected void output(WnSystem sys, HttpClientContext fc) {
        HttpConnector c = fc.context.open();
        WnHttpResponse resp = null;
        try {
            c.prepare();
            c.connect();
            c.sendHeaders();
            c.sendBody();
            resp = c.getResponse();

            // 输出头部
            boolean headerOnly = fc.params.is("H");
            if (fc.params.is("h") || headerOnly) {
                for (Map.Entry<String, Object> en : resp.getHeaders().entrySet()) {
                    String key = en.getKey();
                    Object val = en.getValue();
                    Wlang.each(val, (index, v, src) -> {
                        if (null == key) {
                            sys.out.println(v);
                        } else {
                            sys.out.printlnf("%s: %s", key, v);
                        }
                    });
                }
                sys.out.println();
            }

            if (!headerOnly) {
                sys.out.write(resp);
            }
        }
        catch (Exception e) {
            PrintStream ps = new PrintStream(sys.err.getOutputStream(), true);
            e.printStackTrace(ps);
            throw Er.create(e, "e.cmd.http.send", e.toString());
        }
        finally {
            Streams.safeClose(resp);
        }
    }

    public static NutMap evalQuery(WnSystem sys, ZParams params, boolean readFromStdInput) {
        boolean decode = params.is("decode");
        // 依次读取
        NutMap q = new NutMap();
        if (null != params.vals && params.vals.length > 0) {
            for (String val : params.vals) {
                WnNet.parseQueryTo(q, val, decode);
            }
        }
        // 从文件读取里
        else if (params.has("f")) {
            String ph = params.getString("f");
            WnObj o = Wn.checkObj(sys, ph);
            String input = sys.io.readText(o);
            WnNet.parseQueryTo(q, input, decode);
        }
        // 从标准输入读取
        else if (readFromStdInput) {
            String input = sys.in.readAll();
            WnNet.parseQueryTo(q, input, decode);
        }
        return q;
    }

}
