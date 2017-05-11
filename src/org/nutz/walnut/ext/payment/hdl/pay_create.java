package org.nutz.walnut.ext.payment.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.walnut.ext.payment.WnPayInfo;
import org.nutz.walnut.ext.payment.WnPayObj;
import org.nutz.walnut.ext.payment.WnPayment;
import org.nutz.walnut.ext.payment.WnPays;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class pay_create implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到支付接口
        WnPayment pay = hc.ioc.get(WnPayment.class);

        // 准备创建支付单的信息
        WnPayInfo wpi = WnPays.genPayInfo(hc.params.check("bu"), hc.params.check("se"));

        // 填充简介
        wpi.brief = hc.params.val(0);

        // 填充费用
        WnPays.fillFee(wpi, hc.params.check("fee"));

        // 更多元数据
        if (hc.params.has("meta")) {
            wpi.meta = Lang.map(hc.params.check("meta"));
        }

        // 创建支付单
        WnPayObj po = pay.create(wpi);

        // 回调
        if (hc.params.has("callback")) {
            String callback = hc.params.get("callback");

            // 读取标准输入
            if ("true".equals(callback)) {
                callback = sys.in.readAll();
            }

            // 写入支付单
            pay.setupCallbak(po, callback);
        }

        // 输出
        sys.out.println(Json.toJson(po));
    }

}
