package org.nutz.walnut.impl.box.cmd;

import java.io.InputStream;
import java.io.OutputStream;
import org.nutz.lang.Encoding;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnHttpResponse;
import org.nutz.walnut.util.ZParams;

public class cmd_httpout extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 解析参数
        ZParams params = ZParams.parse(args, "^(debug)$");

        // 准备响应对象头部
        WnHttpResponse resp = new WnHttpResponse(params.getString("headers"));
        resp.setStatus(params.getInt("status", 200));
        resp.setEtag(params.getString("etag"));

        // 准备响应体
        String body = params.getString("body");

        // 来源是标准输入
        if (null == body) {
            if (params.vals.length > 0) {
                byte[] buf = params.val(0).getBytes(Encoding.CHARSET_UTF8);
                resp.prepare(buf);
            }
            // 从管道读取，或者从指定的标准输入读取
            else {
                InputStream ins = sys.in.getInputStream();
                resp.prepare(ins, -1);
            }
        }
        // 输入来自文件对象
        else {
            WnObj wobj = Wn.checkObj(sys, body);
            String range = params.getString("range");
            resp.prepare(sys.io, wobj, range);
        }

        // 输出
        OutputStream ops = sys.out.getOutputStream();
        resp.writeTo(ops);
    }
}
