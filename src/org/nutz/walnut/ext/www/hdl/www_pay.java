package org.nutz.walnut.ext.www.hdl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
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

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax|force)$")
public class www_pay implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        // -------------------------------
        // 站点/账户/密码/票据
        WnObj oWWW = cmd_www.checkSite(sys, hc);
        String orId = hc.params.val_check(1);
        String ticket = hc.params.check("ticket");
        String payType = hc.params.val(2);
        boolean force = hc.params.is("force");

        // -------------------------------
        // 准备服务类
        WnWebService webs = new WnWebService(sys, oWWW);

        // -------------------------------
        // 得到订单
        WnOrder or = webs.getOrderApi().checkOrder(orId);

        // 支付类型变更
        if (null != payType && !payType.equals(or.getPayType())) {
            or.setPayType(payType);
            force = true;
        }

        // -------------------------------
        // 检查会话
        WnAuthSession se = webs.getAuthApi().checkSession(ticket);
        WnAccount bu = se.getMe();

        // -------------------------------
        // 取回支付单
        if (or.hasPayId() && !force) {
            // 得到支付接口
            WnPayment pay = hc.ioc.get(WnPayment.class);
            WnPayObj po = pay.get(or.getPayId(), true);
            if (null != po) {
                or.setPayReturn(po.getPayReturn());
            }
        }
        // -------------------------------
        // 木有支付单的话，创建一个
        if (!or.hasPayReturn()) {
            NutMap upick = hc.params.getAs("upick", NutMap.class);
            cmd_www.prepareToPayOrder(sys, webs, or, bu, upick);
        }

        // -------------------------------
        // 输出结果
        cmd_www.outputOrder(sys, hc, or);
    }

}
