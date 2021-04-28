package org.nutz.walnut.ext.data.www.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.data.www.cmd_www;
import org.nutz.walnut.ext.data.www.bean.OrderPrice;
import org.nutz.walnut.ext.data.www.bean.WnOrder;
import org.nutz.walnut.ext.data.www.impl.WnOrderService;
import org.nutz.walnut.ext.data.www.impl.WnWebService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class www_price implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/账户/密码/票据
        WnObj oWWW = cmd_www.checkSite(sys, hc);

        // -------------------------------
        // 准备服务类
        WnWebService webs = new WnWebService(sys, oWWW);
        WnOrderService orderApi = webs.getOrderApi();

        // -------------------------------
        // 读取订单数据
        String input = sys.in.readAll();

        // -------------------------------
        // 准备订单数据
        WnOrder or = Json.fromJson(WnOrder.class, input);

        // -------------------------------
        // 计算价格
        String priceRuleKey = hc.params.getString("prices", "prices");
        OrderPrice orpri = orderApi.calculatePrice(or, priceRuleKey, null);

        // 输出
        cmd_www.outputJsonOrAjax(sys, orpri, hc);
    }

}
