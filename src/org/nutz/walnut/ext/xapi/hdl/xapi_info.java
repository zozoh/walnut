package org.nutz.walnut.ext.xapi.hdl;

import java.util.Map;

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
        ThirdXApi api = new WnThirdXApi(sys);

        String HR = Strings.dup('=', 60);
        ThirdXExpertManager expertManager = api.getExpertManager();
        Map<String, ThirdXExpert> experts = expertManager.getExperts();
        for (Map.Entry<String, ThirdXExpert> en : experts.entrySet()) {
            ThirdXExpert expert = en.getValue();
            sys.out.println(HR);
            sys.out.printlnf(expert.toString());
        }

    }

}
