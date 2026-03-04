package com.site0.walnut.ext.sys.httpapi.hdl;

import org.nutz.lang.Strings;
import com.site0.walnut.util.tmpl.WnTmpl;
import org.nutz.lang.util.Callback;
import com.site0.walnut.api.err.Er;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.sys.httpapi.HttpApiContext;
import com.site0.walnut.ext.sys.httpapi.HttpApis;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public class httpapi_recall implements JvmHdl {
    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        HttpApis.doApi(sys, hc, new Callback<HttpApiContext>() {
            public void invoke(HttpApiContext c) {
                // 得到请求路径
                String api_path = hc.params.val_check(0);
                // 确保是相对路径
                while (api_path.startsWith("/"))
                    api_path = api_path.substring(1);

                // 获取 API 对象
                WnObj oApi = sys.io.check(c.oApiDir, api_path);

                // .....................................................
                // 得到请求对象
                String req_path = hc.params.val_check(1);
                WnObj oReq = Wn.checkObj(sys, req_path);

                // .....................................................
                // 得到可执行命令模板
                String cmdTmpl = sys.io.readText(oApi);

                // 必须得有内容
                if (Strings.isBlank(cmdTmpl)) {
                    throw Er.create("e.cmd.httapi.emptyApi", oApi);
                }

                // .....................................................
                // 准备执行字符串
                String cmdText = WnTmpl.exec(cmdTmpl, oReq);
                sys.exec(cmdText);
            }
        }, false, true);
    }
}
