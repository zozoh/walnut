package org.nutz.walnut.impl.box.cmd;

import java.io.InputStream;
import java.io.OutputStream;
import org.nutz.lang.Encoding;
import org.nutz.lang.Strings;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.WnHttpResponse;
import org.nutz.walnut.util.ZParams;

public class cmd_httpout extends JvmExecutor {

    private static final String DFT_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                                         + " AppleWebKit/537.36 (KHTML, like Gecko)"
                                         + " Chrome/77.0.3865.90"
                                         + " Safari/537.36";

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 解析参数
        ZParams params = ZParams.parse(args, "^(debug)$");

        // 准备响应对象头部
        WnHttpResponse resp = new WnHttpResponse(params.getString("headers"));
        resp.setStatus(params.getInt("status", 200));
        resp.setEtag(params.getString("etag"));

        // 准备下载
        String downloadName = params.get("download");
        if (!Strings.isBlank(downloadName)) {
            // 指定了特殊的下载名称
            if (!"true".equals(downloadName)) {
                resp.setDownloadName(downloadName);
            }
            // 因为有下载名，所以要设置一下 UA，以便编码下载名
            resp.setUserAgent(params.getString("UserAgent", DFT_UA));
        }
        
        // 指定内容类型
        if(params.has("ct")) {
            resp.setContentType(params.get("ct"));
        }

        // 准备响应体
        String body = params.getString("body");

        // 输入来自文件对象
        if (!Strings.isBlank(body) || "true".equals(body)) {
            WnObj wobj = Wn.checkObj(sys, body);
            String range = params.getString("range");
            resp.prepare(sys.io, wobj, range);
        }
        // 直接在参数里指定了内容
        else if (params.vals.length > 0) {
            String str = Strings.join("\n", params.vals);
            byte[] buf = str.getBytes(Encoding.CHARSET_UTF8);
            resp.prepare(buf);
        }
        // 从管道读取，或者从指定的标准输入读取
        else {
            InputStream ins = sys.in.getInputStream();
            resp.prepare(ins, -1);
        }

        // 输出
        OutputStream ops = sys.out.getOutputStream();
        resp.writeTo(ops);
    }
}
