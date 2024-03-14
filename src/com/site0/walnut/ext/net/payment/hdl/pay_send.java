package com.site0.walnut.ext.net.payment.hdl;

import java.util.Arrays;

import org.nutz.json.Json;
import com.site0.walnut.ext.net.payment.WnPay3xRe;
import com.site0.walnut.ext.net.payment.WnPayObj;
import com.site0.walnut.ext.net.payment.WnPayment;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class pay_send implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到支付接口
        WnPayment pay = hc.ioc.get(WnPayment.class);

        // 得到支付单对象
        String poId = hc.params.val_check(0);
        WnPayObj po = pay.get(poId, false);

        // 得到支付类型
        String payType = hc.params.val_check(1);

        // 得到支付目标s
        String payTarget = hc.params.val_check(2);

        // 得到参数
        String[] args = Arrays.copyOfRange(hc.args, 3, hc.args.length);

        // 发送支付单
        WnPay3xRe re = pay.send(po, payType, payTarget, args);

        // 输出
        sys.out.println(Json.toJson(re));
    }

}
