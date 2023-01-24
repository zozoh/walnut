package org.nutz.walnut.ext.net.xapi.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.net.xapi.XApi;
import org.nutz.walnut.ext.net.xapi.XApiConfigManager;
import org.nutz.walnut.ext.net.xapi.cmd_xapi;
import org.nutz.walnut.ext.net.xapi.bean.XApiRequest;
import org.nutz.walnut.ext.net.xapi.impl.WnXApi;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;

@JvmHdlParamArgs(value = "cqn", regex = "^(force)$")
public class xapi_wxjssdk implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 首先读取变量，变量与会话的变量合并，给上下文更多的信息
        NutMap vars = cmd_xapi.loadVars(sys, hc);
        boolean force = hc.params.is("force");

        // 准备请求路径
        String apiName = hc.params.val_check(0);
        String account = hc.params.val_check(1);
        String path = hc.params.val_check(2);
        String proxyPath = hc.params.getString("proxy");

        // 准备网页的URL
        String url = hc.params.getString("url");
        if (Ws.isBlank(url)) {
            throw Er.create("e.xapi.wxjssdk.NilURL");
        }

        // 准备 API
        XApi api = new WnXApi(sys);
        XApiConfigManager configs = api.getConfigManager();
        NutMap wxconf = configs.loadConfig(apiName, account);
        // 获取应用 ID
        String appID = wxconf.getString("appID");
        if (Ws.isBlank(appID)) {
            throw Er.create("e.xapi.wxjssdk.NilAppID");
        }

        // 准备代理
        xapi_send.setupProxy(sys, proxyPath, api);

        // 获取请求对象
        XApiRequest req = api.prepare(apiName, account, path, vars, force);

        // 发送请求，并且将流输出
        NutMap re = api.send(req, NutMap.class);
        int errcode = re.getInt("errcode", -1);
        if (errcode != 0) {
            throw Er.create("e.xapi.wxjssdk.FailGetTicket", re);
        }
        String ticket = re.getString("ticket");

        // 生成签名
        long timestamp = Wn.now() / 1000;
        String nonceStr = R.sg(16).next();
        String str = String.format("jsapi_ticket=%s&noncestr=%s&timestamp=%d&url=%s",
                                   ticket,
                                   nonceStr,
                                   timestamp,
                                   url);
        String signature = Lang.sha1(str);

        // 返回结果
        NutMap map = new NutMap();
        map.put("appId", appID);
        map.put("timestamp", timestamp);
        map.put("nonceStr", nonceStr);
        map.put("signature", signature);

        JsonFormat jfmt = Cmds.gen_json_format(hc.params);
        String json = Json.toJson(map, jfmt);
        sys.out.print(json);
    }

}
