package org.nutz.walnut.ext.net.http.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.nutz.lang.Streams;
import org.nutz.lang.stream.StringInputStream;
import org.nutz.lang.stream.VoidInputStream;
import org.nutz.walnut.ext.net.http.HttpContext;
import org.nutz.walnut.ext.net.util.WnNet;

public class HttpPostConnector extends AbstractHttpConnector {

    public HttpPostConnector(HttpContext hc) {
        super(hc);
    }

    @Override
    public void sendBody() throws IOException {
        // 标记准备开始 body 部分
        conn.setDoInput(true);
        conn.setDoOutput(true);

        // 准备 body 的输入流
        InputStream ins;
        if (hc.hasBody()) {
            ins = hc.getBody();
        }
        // 将参数表编码传输
        else if (hc.hasParams()) {
            String str = WnNet.toQuery(hc.getParams(), true);
            ins = new StringInputStream(str);
        }
        // 那就是一个空流咯
        else {
            ins = new VoidInputStream();
        }

        // 写入
        OutputStream ops = conn.getOutputStream();
        Streams.writeAndClose(ops, ins);
    }

    @Override
    public void prepare() {}

}
