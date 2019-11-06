package org.nutz.walnut.ext.www.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.bean.WnOrder;
import org.nutz.walnut.ext.www.bean.WnWebSession;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.web.ajax.Ajax;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class www_buy implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/账户/密码/票据
        String site = hc.params.val_check(0);
        String ticket = hc.params.get("ticket");

        // -------------------------------
        // 准备服务类
        WnObj oWWW = Wn.checkObj(sys, site);
        WnObj oDomain = Wn.checkObj(sys, "~/.domain");
        WnWebService webs = new WnWebService(sys, oWWW, oDomain);

        // -------------------------------
        // 检查会话
        WnWebSession se = webs.checkSession(ticket);

        // -------------------------------
        // 读取订单数据
        String input = sys.in.readAll();

        // -------------------------------
        // 准备订单数据
        WnOrder or = Json.fromJson(WnOrder.class, input);
        or.setBuyerId(se.getMe().id());
        or.setAccounts(webs.getAccountHome().id());

        // -------------------------------
        // 执行订单的创建
        or = webs.createOrder(or);

        // -------------------------------
        // 输出结果
        Object re = or.toMeta();
        if (hc.params.is("ajax")) {
            re = Ajax.ok().setData(re);
        }
        String json = Json.toJson(re, hc.jfmt);
        sys.out.println(json);

    }

}
