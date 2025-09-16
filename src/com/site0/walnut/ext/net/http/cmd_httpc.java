package com.site0.walnut.ext.net.http;

import org.nutz.lang.Streams;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.http.bean.WnHttpResponse;
import com.site0.walnut.ext.net.util.WnNet;
import com.site0.walnut.impl.box.JvmFilterExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;
import com.site0.walnut.util.ZParams;
import com.site0.walnut.util.obj.WnObjGetter;

public class cmd_httpc
        extends JvmFilterExecutor<HttpClientContext, HttpClientFilter> {

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
        catch (Throwable e) {
            throw Er.wrap(e);
        }

        // 设置上下文
        fc.context.setUrl(url);
        fc.context.setFollowRedirects(fc.params.is("r"));
        fc.context.setInputStreamFactory(new WnObjGetter(sys));
    }

    @Override
    protected void output(WnSystem sys, HttpClientContext fc) {

        WnHttpResponse resp = null;
        try {
            resp = fc.getRespose();

            // 输出头部
            String hh = fc.outputHeader(resp);
            if (!Ws.isBlank(hh)) {
                sys.out.println(hh);
            }

            boolean headerOnly = fc.shouldOutputHeaderOnly();

            // 输出响应内容
            if (!headerOnly) {
                // 写入到文件
                if (null != fc.oOut) {
                    sys.io.writeAndClose(fc.oOut, resp);
                }
                // 写入到标准输出
                else {
                    sys.out.write(resp);
                }
            }
        }
        catch (Exception e) {
            throw Er.create(e, "e.cmd.httpc.FailToOutput");
        }
        finally {
            Streams.safeClose(resp);
        }
    }

    public static NutMap evalQuery(WnSystem sys,
                                   ZParams params,
                                   boolean readFromStdInput) {
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
