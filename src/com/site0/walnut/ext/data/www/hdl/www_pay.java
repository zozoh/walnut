package com.site0.walnut.ext.data.www.hdl;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.auth.WnAccount;
import com.site0.walnut.api.auth.WnAuthSession;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.data.www.cmd_www;
import com.site0.walnut.ext.data.www.bean.WnOrder;
import com.site0.walnut.ext.data.www.impl.WnOrderService;
import com.site0.walnut.ext.data.www.impl.WnWebService;
import com.site0.walnut.ext.net.payment.WnPayObj;
import com.site0.walnut.ext.net.payment.WnPayment;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

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
        WnOrderService orderApi = webs.getOrderApi();
        WnOrder or = orderApi.checkOrder(orId);

        // 支付类型变更
        if (null != payType && !payType.equals(or.getPayType())) {
            or.setPayType(payType);
            orderApi.checkPayTypeAndSyncSeller(or);
            // 持久化订单数据更新
            NutMap meta = or.toMeta("^(pay_tp|seller)$", null);
            orderApi.updateOrder(or.getId(), meta, sys);
            // 确保一定会重新创建支付单
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
        if (!or.hasPayReturn() || force) {
            NutMap upick = hc.params.getAs("upick", NutMap.class);
            cmd_www.prepareToPayOrder(sys, webs, or, bu, upick);
        }

        // -------------------------------
        // 输出结果
        cmd_www.outputOrder(sys, hc, or);
    }

}
