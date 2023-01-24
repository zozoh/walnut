package org.nutz.walnut.ext.net.xapi.hdl;

import java.util.Map;

import org.nutz.lang.Strings;
import org.nutz.walnut.ext.net.xapi.XApi;
import org.nutz.walnut.ext.net.xapi.XApiExpertManager;
import org.nutz.walnut.ext.net.xapi.bean.XApiExpert;
import org.nutz.walnut.ext.net.xapi.impl.WnXApi;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class xapi_info implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // 准备 API
        XApi api = new WnXApi(sys);

        String HR = Strings.dup('=', 60);
        XApiExpertManager expertManager = api.getExpertManager();
        Map<String, XApiExpert> experts = expertManager.getExperts();
        for (Map.Entry<String, XApiExpert> en : experts.entrySet()) {
            XApiExpert expert = en.getValue();
            sys.out.println(HR);
            sys.out.printlnf(expert.toString());
        }

    }

}
