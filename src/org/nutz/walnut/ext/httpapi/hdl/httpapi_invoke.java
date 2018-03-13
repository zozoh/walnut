package org.nutz.walnut.ext.httpapi.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.castor.Castors;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.tmpl.Tmpl;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.httpapi.HttpApiContext;
import org.nutz.walnut.ext.httpapi.HttpApis;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;

public class httpapi_invoke implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        HttpApis.doApi(sys, hc, new Callback<HttpApiContext>() {
            public void invoke(HttpApiContext c) {
                // 得到请求文件
                String rph = hc.params.val_check(0);

                // 确保是相对路径
                while (rph.startsWith("/"))
                    rph = rph.substring(1);

                // 获取 API 对象
                WnObj oApi = sys.io.check(c.oApiDir, rph);

                // 得到可执行命令模板
                String cmdTmpl = sys.io.readText(oApi);

                // 必须得有内容
                if (Strings.isBlank(cmdTmpl)) {
                    throw Er.create("e.cmd.httapi.emptyApi", oApi);
                }

                // 准备元数据
                NutMap meta = Lang.map("http-method", "GET");
                meta.put("http-usr", c.usr.name());
                meta.put("http-api", rph);
                meta.put("http-protocol", "HTTP/1.1");
                meta.put("expi", System.currentTimeMillis() + 1800000L);

                // 得到请求文件
                WnObj oReq = null;
                String str = hc.params.get("req");
                if (!Strings.isBlank(str)) {
                    oReq = Wn.checkObj(sys, str);
                }
                // 创建
                else {
                    oReq = sys.io.create(c.oApiTmp, "${id}", WnRace.FILE);
                }

                // 写入 query-string
                if (hc.params.has("get")) {
                    String get = hc.params.get("get");
                    if ("@pipe".equals(get)) {
                        get = sys.in.readAll();
                    }
                    NutMap qs = Lang.map(get);
                    if (null != qs) {
                        for (Map.Entry<String, Object> en : qs.entrySet()) {
                            String key = en.getKey();
                            Object val = en.getValue();
                            meta.put("http-qs-" + key, val);
                        }
                    }
                }

                // 写入 cookie
                if (hc.params.has("cookie")) {
                    meta.put("http-header-COOKIE", hc.params.get("cookie"));
                }

                // 更新请求元数据
                sys.io.appendMeta(oReq, meta);

                // 写入请求体
                if (hc.params.has("post")) {
                    String post = hc.params.get("post");
                    if ("@pipe".equals(post)) {
                        post = sys.in.readAll();
                    }
                    NutMap postMap = Lang.map(post);
                    if (null != postMap) {
                        List<String> list = new ArrayList<>(postMap.size());
                        for (Map.Entry<String, Object> en : postMap.entrySet()) {
                            String key = en.getKey();
                            Object val = en.getValue();
                            String vs = Castors.me().castToString(val);
                            list.add(key + "=" + vs);
                        }
                        String postStr = Lang.concat("&", list).toString();
                        sys.io.writeText(oReq, postStr);
                    }
                }
                // 写入请求 body
                else if (hc.params.has("body")) {
                    String body = Cmds.getParamOrPipe(sys, hc.params, "body", true);
                    sys.io.writeText(oReq, body);
                }
                // 根据文件写入 body
                else if (hc.params.has("file")) {
                    String ph = hc.params.get("file");
                    WnObj oFile = Wn.checkObj(sys, ph);
                    sys.io.copyData(oReq, oFile);
                }

                // 准备执行字符串
                String cmdText = Tmpl.exec(cmdTmpl, oReq);
                sys.exec(cmdText);
            }
        }, false, true);
    }

}
