package org.nutz.walnut.ext.xapi.hdl;

import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.xapi.ThirdXApi;
import org.nutz.walnut.ext.xapi.ThirdXExpertManager;
import org.nutz.walnut.ext.xapi.bean.ThirdXExpert;
import org.nutz.walnut.ext.xapi.impl.WnThirdXApi;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class xapi_info implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备 API
        ThirdXApi api = new WnThirdXApi(sys.io, sys);

        String HR = Strings.dup('=', 60);
        ThirdXExpertManager experts = api.getExperts();
        for (Map.Entry<String, ThirdXExpert> en : experts.getExperts().entrySet()) {
            String apiName = en.getKey();
            ThirdXExpert expert = en.getValue();
            sys.out.println(HR);
            sys.out.printlnf("@export: %s", apiName);
            String json = Json.toJson(expert);
            sys.out.println(json);
        }

    }

}
