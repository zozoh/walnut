package org.nutz.walnut.ext.payment.hdl;

import org.nutz.json.Json;
import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.ext.payment.WnPay3xRe;
import org.nutz.walnut.ext.payment.WnPayObj;
import org.nutz.walnut.ext.payment.WnPayment;
import org.nutz.walnut.ext.payment.WnPays;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("s")
public class pay_re implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到支付接口
        WnPayment pay = hc.ioc.get(WnPayment.class);

        // 得到回调请求参数
        String json = hc.params.val(1);

        if (Strings.isBlank(json)) {
            json = sys.in.readAll();
        }
        NutMap req = Lang.map(json);

        // 得到支付单对象
        String poIdKey = hc.params.check("idkey");
        String poId = req.getString(poIdKey);
        WnPayObj po = null;
        if (Strings.isBlank(poId))
            throw Er.create("e.cmd.pay.re.noPoId");

        // 强制获取支付单
        po = pay.get(poId, false);

        // 完成支付单
        WnPay3xRe re = pay.complete(po, req);

        // 看看有没有必要调用回调
        WnPays.try_callback(sys, po);

        // 输出
        if (hc.params.is("s"))
            sys.out.println("success");
        else
            sys.out.println(Json.toJson(re));
    }

}
