package org.nutz.walnut.ext.payment.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.payment.WnPay3xRe;
import org.nutz.walnut.ext.payment.WnPayObj;
import org.nutz.walnut.ext.payment.WnPayment;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class pay_re implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到支付接口
        WnPayment pay = hc.ioc.get(WnPayment.class);

        // 得到支付单对象
        String poId = hc.params.val_check(0);
        WnPayObj po = pay.get(poId, false);

        // 得到回调请求参数
        NutMap req;
        String json = hc.params.val(1);

        if (Strings.isBlank(json)) {
            req = Lang.map(json);
        } else {
            req = new NutMap();
        }

        // 完成支付单
        WnPay3xRe re = pay.complete(po, req);

        // 输出
        sys.out.println(Json.toJson(re));
    }

}
