package org.nutz.walnut.ext.www.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.cmd_www;
import org.nutz.walnut.ext.www.bean.WnOrder;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

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
        WnWebService webs = new WnWebService(sys, oWWW);

        // -------------------------------
        // 检查会话
        WnAuthSession se = webs.getAuthApi().checkSession(ticket);
        WnAccount bu = se.getMe();
        WnObj oAccontHome = webs.getSite().getAccountHome();

        // -------------------------------
        // 读取订单数据
        String input = sys.in.readAll();

        // -------------------------------
        // 准备订单数据
        WnOrder or = Json.fromJson(WnOrder.class, input);
        or.setBuyerId(bu.getId());
        or.setAccounts(oAccontHome.id());

        // -------------------------------
        // 执行订单的创建
        or = webs.getOrderApi().createOrder(or);

        // -------------------------------
        // 准备支付单
        cmd_www.prepareToPayOrder(sys, webs, or, bu);

        // -------------------------------
        // 输出结果
        cmd_www.outputOrder(sys, hc, or);
    }

}