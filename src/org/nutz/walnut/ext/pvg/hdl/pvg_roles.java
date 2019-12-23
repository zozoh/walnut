package org.nutz.walnut.ext.pvg.hdl;

import java.util.List;
import org.nutz.json.Json;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.pvg.BizPvgService;
import org.nutz.walnut.ext.pvg.cmd_pvg;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class pvg_roles implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到服务类
        BizPvgService pvgs = cmd_pvg.getPvgService(hc);
        boolean JSON = hc.params.is("json");
        List<String> list = pvgs.getRoleNames();

        // JSON 模式输出
        if (JSON) {
            sys.out.println(Json.toJson(list, hc.jfmt));
        }
        // 默认输出
        else if (!list.isEmpty()) {
            sys.out.println(Strings.join(", ", list));
        }
        // 默认输出空
        else {
            sys.out.println("--No Roles--");
        }
    }

}
