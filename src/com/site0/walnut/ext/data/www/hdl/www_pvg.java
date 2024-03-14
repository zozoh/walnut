package com.site0.walnut.ext.data.www.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

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
