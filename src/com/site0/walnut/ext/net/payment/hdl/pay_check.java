package com.site0.walnut.ext.net.payment.hdl;

import org.nutz.json.Json;
import com.site0.walnut.ext.net.payment.WnPay3xRe;
import com.site0.walnut.ext.net.payment.WnPayObj;
import com.site0.walnut.ext.net.payment.WnPayment;
import com.site0.walnut.ext.net.payment.WnPays;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class pay_check implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到支付接口
        WnPayment pay = hc.ioc.get(WnPayment.class);

        // 得到支付单对象
        String poId = hc.params.val_check(0);
        WnPayObj po = pay.get(poId, false);

        // 执行状态检查
        WnPay3xRe re = pay.check(po);

        // 看看有没有必要调用回调
        WnPays.try_callback(sys, po);

        // 输出
        sys.out.println(Json.toJson(re));
    }

}
