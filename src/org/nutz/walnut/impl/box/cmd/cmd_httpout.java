package org.nutz.walnut.impl.box.cmd;

import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.sshd.common.util.io.LimitInputStream;
import org.nutz.http.Http;
import org.nutz.lang.Encoding;
import org.nutz.lang.Lang;
import org.nutz.lang.Streams;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.mvc.view.RawView;
import org.nutz.mvc.view.RawView.RangeRange;
import org.nutz.mvc.view.RawView2;
import org.nutz.walnut.api.io.WnObj;
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
        String _etag = params.get("etag");
        String range = params.get("range");
        String etag = null;
        byte[] buf = null;
        InputStream ins = null;

        NutMap headers = Strings.isBlank(headers_str) ? new NutMap() : Lang.map(headers_str);
        try {
            if (body == null) {
                if (params.vals.length > 0)
                    buf = params.val(0).getBytes(Encoding.CHARSET_UTF8);
                // 从管道读取，或者从指定的标准输入读取
                else {
                    ins = sys.in.getInputStream();
                }
            } else {
                body = Wn.normalizeFullPath(body, sys);
                WnObj wobj = sys.io.check(null, body);
                // 304 问题
                if (!Strings.isBlank(_etag)) {
                    etag = Wn.getEtag(wobj);
                    if (_etag.equalsIgnoreCase(etag)) {
                        // 看来是一样的哦, 304待命
                        print304(sys, wobj);
                        return;
                    }
                    ins = sys.io.getInputStream(wobj, 0);
                    headers.put("ETag", etag);
                    // 断点续传
                    if (!Strings.isBlank(range)) {
                        List<RangeRange> rs = new ArrayList<RawView.RangeRange>();
                        if (!RawView2.parseRange(range, rs, (int)wobj.len())) {
                            sys.out.println("HTTP/1.1 Range Not Satisfiable");
                            sys.out.println();
                            return;
                        }
                        if (rs.size() != 1) {
                            sys.out.println("HTTP/1.1 Range Not Satisfiable");
                            sys.out.println();
                            return;
                        }
                        RangeRange rr = rs.get(0);
                        headers.put("Content-Length", rr.end - rr.start);
                        headers.put("Accept-Ranges", "bytes");
                        headers.put("Content-Range", String.format("bytes %d-%d/%d",
                                                                   rr.start,
                                                                   rr.end - 1,
                                                                   wobj.len()));
                        status = 206;
                        ins.skip(rr.start);
                        ins = new LimitInputStream(ins, rr.end - rr.start);
                    }
                }
            }

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
            else if (ins != null)
                Streams.writeAndClose(sys.out.getOutputStream(), ins);
        }
        finally {
            Streams.safeClose(ins);
        }
    }

    public static void print304(WnSystem sys, WnObj wobj) {
        sys.out.println("HTTP/1.1 304 Not Modified");
        if (wobj != null)
            sys.out.println("Walnut-Object-Id: " + wobj.id());
        sys.out.println();
    }
}
