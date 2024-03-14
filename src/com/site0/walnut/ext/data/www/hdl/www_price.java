package com.site0.walnut.ext.data.www.hdl;

import org.nutz.json.Json;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.www.cmd_www;
import com.site0.walnut.ext.data.www.bean.OrderPrice;
import com.site0.walnut.ext.data.www.bean.WnOrder;
import com.site0.walnut.ext.data.www.impl.WnOrderService;
import com.site0.walnut.ext.data.www.impl.WnWebService;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

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
