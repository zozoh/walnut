package org.nutz.walnut.impl.box.cmd;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.util.Map.Entry;

import org.nutz.http.Http;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmExecutor;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class cmd_httpout extends JvmExecutor {

    @Override
    public void exec(WnSystem sys, String[] args) throws Exception {
        ZParams params = ZParams.parse(args, "debug");
        int status = params.getInt("status", 200);
        String status_text = params.getString("status_text", Http.getStatusText(status, "FUCK"));
        String headers_str = params.getString("headers");
        String body = params.get("body");
        byte[] buf = null;
        if (body == null) {
            if (params.vals.length > 0)
                buf = params.val(0).getBytes(Encoding.CHARSET_UTF8);
            // 从管道读取，或者从指定的标准输入读取
            else {
                buf = Streams.readBytes(sys.in.getInputStream());
            }
        } else {
            ByteArrayOutputStream ops = new ByteArrayOutputStream();
            sys.io.readAndClose(sys.io.check(null, Wn.normalizeFullPath(body, sys)), ops);
            buf = ops.toByteArray();
        }

        NutMap headers = Strings.isBlank(headers_str) ? new NutMap() : Lang.map(headers_str);

        if (status / 100 == 3) {
            buf = null;
            headers.remove("Content-Length");
        }
        if (buf != null && !headers.containsKey("Content-Length")) {
            headers.put("Content-Length", buf.length);
        }
        // 开始生成http响应了

        // 首先的状态行
        sys.out.printlnf("HTTP/1.1 %d %s", status, status_text);
        // 然后是headers
        sys.out.println("X-Power-By: Walnut");
        for (Entry<String, Object> en : headers.entrySet()) {
            sys.out.print(URLEncoder.encode(en.getKey(), "UTF-8"));
            sys.out.print(": ");
            // sys.out.println(URLEncoder.encode(""+en.getValue(), "UTF-8"));
            sys.out.println("" + en.getValue());
        }
        sys.out.println();
        if (buf != null)
            sys.out.getOutputStream().write(buf);
    }

}
