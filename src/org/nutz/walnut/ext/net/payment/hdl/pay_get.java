package org.nutz.walnut.ext.net.payment.hdl;

import org.nutz.json.Json;
import org.nutz.walnut.ext.net.payment.WnPayObj;
import org.nutz.walnut.ext.net.payment.WnPayment;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class pay_get implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到支付接口
        WnPayment pay = hc.ioc.get(WnPayment.class);

        // 得到支付单对象
        String poId = hc.params.val_check(0);
        WnPayObj po = pay.get(poId, false);

        // 输出
        sys.out.println(Json.toJson(po));
    }

}
