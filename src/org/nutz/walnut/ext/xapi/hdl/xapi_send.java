package org.nutz.walnut.ext.xapi.hdl;

import java.io.InputStream;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.xapi.ThirdXApi;
import org.nutz.walnut.ext.xapi.bean.ThirdXRequest;
import org.nutz.walnut.ext.xapi.impl.WnThirdXApi;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

public class xapi_send implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 首先读取变量
        String str = Cmds.getParamOrPipe(sys, hc.params, "vars", true);
        NutMap vars = Strings.isBlank(str) ? new NutMap() : Lang.map(str);

        // 准备请求路径
        String apiName = hc.params.val_check(0);
        String account = hc.params.val_check(1);
        String path = hc.params.val_check(2);

        // 准备 API
        ThirdXApi api = new WnThirdXApi(sys.io, sys);

        // 获取请求对象
        ThirdXRequest req = api.prepare(apiName, account, path, vars);

        // 解析请求参数
        req.explainHeaders(vars);
        req.explainParams(vars);

        // TODO 这里将来搞搞更多灵活的 body，譬如 XML/JSON/FileUpload 等

        // 发送请求，并且将流输出
        InputStream ins = api.send(req, InputStream.class);
        sys.out.writeAndClose(ins);

    }

}
