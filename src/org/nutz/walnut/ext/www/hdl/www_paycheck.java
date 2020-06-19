package org.nutz.walnut.ext.www.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.auth.WnAuthSession;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.payment.WnPay3xRe;
import org.nutz.walnut.ext.www.cmd_www;
import org.nutz.walnut.ext.www.bean.WnOrder;
import org.nutz.walnut.ext.www.impl.WnWebService;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class www_paycheck implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // -------------------------------
        // 站点/账户/密码/票据
        WnObj oWWW = cmd_www.checkSite(sys, hc);
        String orId = hc.params.val_check(1);
        String ticket = hc.params.check("ticket");

        // -------------------------------
        // 准备服务类
        WnWebService webs = new WnWebService(sys, oWWW);

        // -------------------------------
        // 检查会话
        WnAuthSession se = webs.getAuthApi().checkSession(ticket);
        WnAccount bu = se.getMe();

        // -------------------------------
        // 获取订单对象
        WnObj oOrder = sys.io.checkById(orId);

        WnOrder or = new WnOrder();
        or.updateBy(oOrder);

        // 确保是自己
        if (!bu.isSameId(or.getBuyerId())) {
            throw Er.create("e.www.paycheck.NotMine", orId);
        }

        // -------------------------------
        // 获取支付单
        if (or.hasPayId()) {
            String payId = or.getPayId();
            String cmdText = String.format("pay check %s", payId);
            String re = sys.exec2(cmdText);
            WnPay3xRe payRe = Json.fromJson(WnPay3xRe.class, re);

            // 重新获取支付单信息
            oOrder = sys.io.checkById(orId);
            or = new WnOrder();
            or.updateBy(oOrder);

            // 更新状态
            if (or.hasPayReturn()) {
                WnPay3xRe oldRe = or.getPayReturn();
                oldRe.setStatus(payRe.getStatus());
                oldRe.setErrMsg(payRe.getErrMsg());
            }
            // 反正也没有，就设一下咯
            else {
                or.setPayReturn(payRe);
            }

            // 持久化一下
            sys.io.appendMeta(oOrder, or.toMeta("^pay_re$", null));
        }

        // -------------------------------
        // 输出结果
        cmd_www.outputOrder(sys, hc, or);
    }

}
