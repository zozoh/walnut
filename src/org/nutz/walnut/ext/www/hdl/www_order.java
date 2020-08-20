package org.nutz.walnut.ext.www.hdl;

import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.payment.WnPayObj;
import org.nutz.walnut.ext.payment.WnPayment;
import org.nutz.walnut.ext.www.cmd_www;
import org.nutz.walnut.ext.www.bean.WnOrder;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class www_order implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/账户/密码/票据
        String site = hc.params.val_check(0);
        String ticket = hc.params.get("ticket");
        String unm = hc.params.get("u");
        String orId = hc.params.val_check(1);

        // -------------------------------
        // 准备服务类
        WnObj oWWW = cmd_www.checkSite(sys, site);
        WnWebService webs = new WnWebService(sys, oWWW);
        // -------------------------------
        // 确定用户
        WnAccount u = cmd_www.checkTargetUser(sys, webs, unm, ticket);

        // -------------------------------
        // 得到订单
        WnOrder or = webs.getOrderApi().checkOrder(orId);

        // 如果订单不是所属用户的，返回空
        if (null != or && !or.getBuyerId().equals(u.getId())) {
            or = null;
        }

        // -------------------------------
        // 取回支付单
        if (or.hasPayId()) {
            // 得到支付接口
            WnPayment pay = hc.ioc.get(WnPayment.class);
            WnPayObj po = pay.get(or.getPayId(), true);
            if (null != po) {
                or.setPayReturn(po.getPayReturn());
            }
        }

        // -------------------------------
        // 输出结果
        cmd_www.outputOrder(sys, hc, or);
    }

}
