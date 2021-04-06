package org.nutz.walnut.ext.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cqn")
public class www_pvg implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {

        // 权限表
        NutBean myAvaPvg = sys.getAllMyPvg();

        // 输出
        String json = Json.toJson(myAvaPvg, hc.jfmt);
        sys.out.println(json);
    }

}
