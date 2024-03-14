package com.site0.walnut.ext.data.pvg.hdl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.nutz.json.Json;
import org.nutz.lang.Strings;
import com.site0.walnut.ext.data.pvg.BizPvgService;
import com.site0.walnut.ext.data.pvg.cmd_pvg;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import org.nutz.web.ajax.Ajax;
import org.nutz.web.ajax.AjaxReturn;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class pvg_can implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到服务类
        BizPvgService pvgs = cmd_pvg.getPvgService(hc);
        boolean isOr = hc.params.is("or");

        // 得到角色名
        String role = hc.params.val_check(0);

        // 得到要检测的行为列表
        String[] actions = Arrays.copyOfRange(hc.params.vals, 1, hc.params.vals.length);

        // 准备返回值
        Map<String, Boolean> data = new HashMap<>();
        String[] roles = Strings.splitIgnoreBlank(role);
        boolean ok = false;
        for (String _role : roles) {
            ok |= pvgs.can(data, isOr, _role, actions);
            if (ok)
                break;
        }

        // 输出
        Object re = ok;
        if (hc.params.is("ajax")) {
            AjaxReturn rex = ok ? Ajax.ok() : Ajax.fail();
            rex.setData(data);
            re = rex;
        }
        String outJson = Json.toJson(re, hc.jfmt);
        sys.out.println(outJson);
    }

}
