package org.nutz.walnut.impl.box.cmd;

import org.nutz.http.Request;
import org.nutz.http.Sender;
import org.nutz.http.Request.METHOD;
import org.nutz.http.Response;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_curl extends JvmExecutor {

    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "^(v|debug)$");
        String url = params.val_check(0);
        Request req = Request.create(url, METHOD.GET);

        // 处理data参数
        if (params.has("data")) {
            req.setMethod(METHOD.POST);
            req.setData(params.get("data"));
        }
        // 处理f参数
        String fbody = params.get("f");
        if (params.has("f")) {
            req.setMethod(METHOD.POST);
            req.setInputStream(sys.io.getInputStream(sys.io.check(null,
                                                                  Wn.normalizeFullPath(fbody, sys)),
                                                     0));
        }
        // 处理 X 和 request参数
        if (params.has("X")) {
            req.setMethod(METHOD.valueOf(params.get("X").toUpperCase()));
        } else if (params.has("request")) {
            req.setMethod(METHOD.valueOf(params.get("request").toUpperCase()));
        }
        // 处理header
        if (params.has("header")) {
            for (String header : params.get("header").split(";")) {
                req.getHeader().set(header.split(":")[0], header.split(":")[1]);
            }
        }
        Sender sender = Sender.create(req);
        // 处理connect-timeout
        if (params.has("connect-timeout")) {
            sender.setTimeout(params.getInt("connect-timeout"));
        }
        Response resp = sender.send();
        if (resp.isOK()) {
            sys.out.write(resp.getStream());
        } else {
            sys.err.print("code = " + resp.getStatus());
        }
    }

}