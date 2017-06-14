package org.nutz.walnut.ext.payment.hdl;

import org.nutz.json.Json;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.walnut.ext.payment.WnPay3xRe;
import org.nutz.walnut.ext.payment.WnPayInfo;
import org.nutz.walnut.ext.payment.WnPayObj;
import org.nutz.walnut.ext.payment.WnPayment;
import org.nutz.walnut.ext.payment.WnPays;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;

public class pay_create implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到支付接口
        WnPayment pay = hc.ioc.get(WnPayment.class);

        // 准备创建支付单的信息
        WnPayInfo wpi = WnPays.genPayInfo(hc.params.check("bu"), hc.params.get("se"));

        // 填充简介
        wpi.brief = hc.params.get("br");

        // 填充费用
        WnPays.fillFee(wpi, hc.params.check("fee"));

        // 回调
        if (hc.params.has("callback")) {
            wpi.callbackName = hc.params.get("callback");
        }

        // 更多元数据
        String json = Cmds.getParamOrPipe(sys, hc.params, "meta", false);
        if (!Strings.isBlank(json)) {
            wpi.meta = Lang.map(json);
        }

        // 创建支付单
        WnPayObj po = pay.create(wpi);

        // 继续发送支付单
        if (hc.params.has("pt")) {
            // 得到支付类型
            String payType = hc.params.get("pt");

            // 得到支付目标s
            String payTarget = hc.params.check("ta");

            // 得到参数
            String[] args = hc.params.vals;

            // 发送支付单
            WnPay3xRe re = pay.send(po, payType, payTarget, args);

            // 输出发送结果
            sys.out.println(Json.toJson(re,
                                        JsonFormat.nice()
                                                  .setQuoteName(true)
                                                  .setIgnoreNull(false)
                                                  .setLocked("^(changedKeys)$")));
        }
        // 输出
        else {
            sys.out.println(Json.toJson(po));
        }
    }

}
