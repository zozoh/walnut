package com.site0.walnut.ext.net.xapi.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.ext.net.xapi.XApi;
import com.site0.walnut.ext.net.xapi.cmd_xapi;
import com.site0.walnut.ext.net.xapi.bean.XApiRequest;
import com.site0.walnut.ext.net.xapi.impl.WnXApi;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Ws;

@JvmHdlParamArgs(value = "cnqN", regex = "^(url|force)$")
public class xapi_req implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 首先读取变量，变量与会话的变量合并，给上下文更多的信息
        NutBean vars = cmd_xapi.loadVars(sys, hc);

        // 准备请求路径
        String apiName = hc.params.val_check(0);
        String account = hc.params.val_check(1);
        String path = hc.params.val_check(2);
        boolean force = hc.params.is("force");
        boolean sameLine = !hc.params.is("N");

        // 准备 API
        XApi api = new WnXApi(sys);

        // 获取请求对象
        XApiRequest req = api.prepare(apiName, account, path, vars, force);

        // 打印请求 URL 完整路径
        if (hc.params.is("curl")) {
            sys.out.println(req.toCURLCommand(sameLine));
        }
        // 打印请求 CURL 的命令
        else if (hc.params.is("url")) {
            sys.out.println(req.toUrl());
        }
        // 打印请求的缓存键
        else if (hc.params.is("cache")) {
            sys.out.println(Ws.sBlank(req.checkCacheKey(), "-no-key-"));
        }
        // 打印请求对象
        else {
            String json = Json.toJson(req, hc.jfmt);
            sys.out.println(json);
        }

    }

}
