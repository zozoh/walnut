package org.nutz.walnut.ext.xapi.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.xapi.ThirdXApi;
import org.nutz.walnut.ext.xapi.cmd_xapi;
import org.nutz.walnut.ext.xapi.bean.ThirdXRequest;
import org.nutz.walnut.ext.xapi.impl.WnThirdXApi;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cnq", regex = "^(url)$")
public class xapi_req implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 首先读取变量，变量与会话的变量合并，给上下文更多的信息
        NutMap vars = cmd_xapi.loadVars(sys, hc);

        // 准备请求路径
        String apiName = hc.params.val_check(0);
        String account = hc.params.val_check(1);
        String path = hc.params.val_check(2);

        // 准备 API
        ThirdXApi api = new WnThirdXApi(sys);

        // 获取请求对象
        ThirdXRequest req = api.prepare(apiName, account, path, vars);

        // 打印请求 URL 完整路径
        if (hc.params.is("url")) {
            sys.out.println(req.toUrl(true));
        }
        // 打印请求对象
        else {
            String json = Json.toJson(req, hc.jfmt);
            sys.out.println(json);
        }

    }

}
