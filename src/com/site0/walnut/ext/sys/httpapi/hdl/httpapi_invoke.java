package com.site0.walnut.ext.sys.httpapi.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.nutz.castor.Castors;
import org.nutz.json.Json;
import com.site0.walnut.util.Wlang;
import org.nutz.lang.Strings;
import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.Callback;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.ext.sys.httpapi.HttpApiContext;
import com.site0.walnut.ext.sys.httpapi.HttpApis;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.Ws;

@JvmHdlParamArgs("cqn")
public class httpapi_invoke implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        HttpApis.doApi(sys, hc, new Callback<HttpApiContext>() {
            public void invoke(HttpApiContext c) {
                // 得到请求文件
                String rph = hc.params.val_check(0);
                // .....................................................
                // 读取请求参数
                NutMap qs = __load_param_map(sys, hc, "get");
                NutMap postMap = __load_param_map(sys, hc, "post");

                // 确保是相对路径
                while (rph.startsWith("/"))
                    rph = rph.substring(1);

                // 获取 API 对象
                WnObj oApi = sys.io.check(c.oApiDir, rph);

                // .....................................................
                // 如果 api 声明了 pa_cnd_keys
                String cndKey = hc.params.get("ncnd");
                if (!Strings.isBlank(cndKey) && oApi.has(cndKey)) {
                    String pa_cnd_keys = oApi.getString(cndKey);
                    NutMap qs_cnd = qs.pickBy(pa_cnd_keys);
                    NutMap po_cnd = postMap.pickBy(pa_cnd_keys);
                    if (qs_cnd.isEmpty() && po_cnd.isEmpty()) {
                        NutMap re = Wlang.map("ok", false);
                        re.put("errCode", "e.cmd.httpapi.invoke.nocnd");
                        sys.err.println(Json.toJson(re, hc.jfmt));
                        return;
                    }
                }

                // .....................................................
                // 得到可执行命令模板
                String cmdTmpl = sys.io.readText(oApi);

                // 必须得有内容
                if (Strings.isBlank(cmdTmpl)) {
                    throw Er.create("e.cmd.httapi.emptyApi", oApi);
                }
                // .....................................................
                // 准备元数据
                NutMap meta = Wlang.map("http-method", "GET");
                meta.put("http-usr", c.usr.getName());
                meta.put("http-api", rph);
                meta.put("http-protocol", "HTTP/1.1");
                meta.put("expi", Wn.now() + 1800000L);
                // .....................................................
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
                // .....................................................
                // 写入 query-string
                if (qs.size() > 0) {
                    for (Map.Entry<String, Object> en : qs.entrySet()) {
                        String key = en.getKey();
                        Object val = en.getValue();
                        meta.put("http-qs-" + key, val);
                    }
                }
                // .....................................................
                // 写入 cookie
                if (hc.params.has("cookie")) {
                    meta.put("http-header-COOKIE", hc.params.get("cookie"));
                }
                // .....................................................
                // 更新请求元数据
                sys.io.appendMeta(oReq, meta);
                // .....................................................
                // 写入请求体
                if (postMap.size() > 0) {
                    // 修改请求方法
                    meta.put("http-method", "POST");

                    // 写入请求体
                    List<String> list = new ArrayList<>(postMap.size());
                    for (Map.Entry<String, Object> en : postMap.entrySet()) {
                        String key = en.getKey();
                        Object val = en.getValue();
                        String vs = Castors.me().castToString(val);
                        list.add(key + "=" + vs);
                    }
                    String postStr = Ws.join(list, "&").toString();
                    sys.io.writeText(oReq, postStr);
                }
                // 写入请求 body
                else if (hc.params.has("body")) {
                    // 修改请求方法
                    meta.put("http-method", "POST");
                    // 得到请求体
                    String body = Cmds.getParamOrPipe(sys, hc.params, "body", true);
                    sys.io.writeText(oReq, body);
                }
                // 根据文件写入 body
                else if (hc.params.has("file")) {
                    // 修改请求方法
                    meta.put("http-method", "POST");
                    // 得到请求体
                    String ph = hc.params.get("file");
                    WnObj oFile = Wn.checkObj(sys, ph);
                    sys.io.copyData(oReq, oFile);
                }
                // .....................................................
                // 准备执行字符串
                String cmdText = WnTmpl.exec(cmdTmpl, oReq);
                sys.exec(cmdText);
            }
        }, false, true);
    }

    private NutMap __load_param_map(WnSystem sys, JvmHdlContext hc, String pa_key) {
        NutMap qs;
        if (hc.params.has(pa_key)) {
            String get = hc.params.get(pa_key);
            if ("@pipe".equals(get)) {
                get = sys.in.readAll();
            }
            qs = Wlang.map(get);
        } else {
            qs = new NutMap();
        }
        return qs;
    }

}
