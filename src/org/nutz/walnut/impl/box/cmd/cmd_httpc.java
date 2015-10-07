package org.nutz.walnut.impl.box.cmd;

import java.io.InputStream;

import org.nutz.http.Http;
import org.nutz.http.Request;
import org.nutz.http.Response;
import org.nutz.http.Sender;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_httpc extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 分析参数
        String method;
        String url;

        ZParams params = ZParams.parse(args, null);
        String arg0 = params.vals[0];

        // 默认，如果有 body 就 POST 否则 GET
        if (arg0.startsWith("http")) {
            method = params.has("body") ? "POST" : "GET";
            url = arg0;
        }
        // GET/POST
        else if (arg0.toUpperCase().matches("^(GET|POST)$")) {
            method = arg0.toUpperCase();
            url = params.vals[1];
        }
        // 靠，不支持
        else {
            throw Er.create("e.cmd.httpc.unsupportMethod", arg0);
        }

        // GET 就发送
        if ("GET".equals(method)) {
            String re = Http.get(url).getContent();
            sys.out.println(re);
            return;
        }
        // POST 分析 Body
        if ("POST".equals(method)) {
            Response resp;

            // 声明了 body
            String body = params.get("body");
            // 从标准输入得到内容
            if (Strings.isBlank(body)) {
                Request req = Request.create(url, Request.METHOD.POST);
                req.setInputStream(sys.in.getInputStream());
                Sender sender = Sender.create(req);
                resp = sender.send();
            }
            // 是个 JSON 数据
            else if (Strings.isQuoteBy(body, '{', '}')) {
                Sender sender = Sender.create(Request.create(url, Request.METHOD.POST, body));
                resp = sender.send();
            }
            // 应该是个文件
            else {
                WnObj oBody = Wn.checkObj(sys, body);
                Request req = Request.create(url, Request.METHOD.POST);
                req.getHeader().set("Content-Type", oBody.mime());
                InputStream ins = sys.io.getInputStream(oBody, 0);
                try {
                    req.setInputStream(ins);
                    Sender sender = Sender.create(req);
                    resp = sender.send();
                }
                finally {
                    Streams.safeClose(ins);
                }
            }
            // 处理返回
            sys.out.println(resp.getContent());
        }
    }

}
