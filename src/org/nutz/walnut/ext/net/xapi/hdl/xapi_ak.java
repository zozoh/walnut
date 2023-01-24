package org.nutz.walnut.ext.net.xapi.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.ext.net.xapi.XApi;
import org.nutz.walnut.ext.net.xapi.impl.WnXApi;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

@JvmHdlParamArgs("cqn")
public class xapi_ak implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备请求路径
        String apiName = hc.params.val_check(0);
        String account = hc.params.val_check(1);

        // 准备 API
        XApi api = new WnXApi(sys);

        // 判断
        boolean re = api.hasValidAccessKey(apiName, account);

        // 输出
        AjaxReturn ar = Ajax.ok().setOk(re);
        String json = Json.toJson(ar, hc.jfmt);
        sys.out.println(json);
    }

}
