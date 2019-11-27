package org.nutz.walnut.ext.www.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.www.cmd_www;
import org.nutz.walnut.ext.www.bean.WnOrder;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs(value = "cqn", regex = "^(ajax)$")
public class www_paycheck implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // -------------------------------
        // 获取订单对象
        String orId = hc.params.val_check(0);
        WnObj oOrder = sys.io.checkById(orId);

        WnOrder or = new WnOrder();
        or.updateBy(oOrder);

        // -------------------------------
        // 获取支付单
        if (or.hasPayId()) {
            String payId = or.getPayId();
            String cmdText = String.format("pay check %s", payId);
            sys.exec2(cmdText);

            // 重新获取支付单信息
            oOrder = sys.io.checkById(orId);
            or = new WnOrder();
            or.updateBy(oOrder);
        }

        // -------------------------------
        // 输出结果
        cmd_www.outputOrder(sys, hc, or);
    }

}
