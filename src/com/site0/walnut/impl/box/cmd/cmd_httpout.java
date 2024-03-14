package com.site0.walnut.impl.box.cmd;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import org.nutz.lang.Encoding;
import org.nutz.lang.Strings;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.core.bm.localbm.LocalIoBM;
import com.site0.walnut.impl.box.JvmExecutor;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnHttpResponseWriter;
import com.site0.walnut.util.ZParams;

public class cmd_httpout extends JvmExecutor {

    private static final String DFT_UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64)"
                                         + " AppleWebKit/537.36 (KHTML, like Gecko)"
                                         + " Chrome/77.0.3865.90"
                                         + " Safari/537.36";

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        // 解析参数
        ZParams params = ZParams.parse(args, "^(debug|base64)$");

        // 准备响应对象头部
        WnHttpResponseWriter resp = new WnHttpResponseWriter(params.getString("headers"));
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

        // 指明了内容类型
        boolean asBase64 = params.is("base64");
        resp.setAsBase64(asBase64);
        String mime = params.getString("mime");
        if (!Strings.isBlank(mime)) {
            resp.setContentType(mime);
        }

        // 准备响应体
        String body = params.getString("body");

        // 输入来自文件对象
        if (!Strings.isBlank(body)) {
            String range = params.getString("range");
            // 如果是一个SHA1 指纹
            // 凑合先用全局桶支应一阵
            if (body.startsWith("sha1:")) {
                String sha1 = body.substring(5).trim();
                // 兼容一下，有些时候，真实生产，客户端会将其变成 xxxx/xxx... 的路径形式
                sha1 = sha1.replaceAll("[/-]", "");
                LocalIoBM bm = this.ioc.get(LocalIoBM.class, "globalBM");
                File f = bm.getBucketFile(sha1);
                if (!f.exists()) {
                    throw Er.create("e.io.bm.global.noexist", sha1);
                }
                resp.prepare(f, sha1, range);
            }
            // 那么就是一个对象的路径咯
            else {
                WnObj wobj = Wn.checkObj(sys, body);
                resp.prepare(sys.io, wobj, range);
            }
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
