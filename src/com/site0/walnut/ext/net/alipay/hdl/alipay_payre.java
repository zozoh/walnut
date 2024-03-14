package com.site0.walnut.ext.net.alipay.hdl;

import java.util.Map;

import org.nutz.json.Json;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.ext.net.alipay.AlipayConfig;
import com.site0.walnut.ext.net.alipay.AlipayNotify;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

/**
 * httpparam -in id:${id} | alipay xxx payre
 */
public class alipay_payre implements JvmHdl {

    @SuppressWarnings("unchecked")
    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 读取支付宝配置信息
        WnObj tmp = hc.getAs("alipayconf_obj", WnObj.class);
        AlipayConfig alipayConfig = sys.io.readJson(tmp, AlipayConfig.class);
        Map<String, String> params = Json.fromJson(Map.class, sys.in.getReader());
        if (AlipayNotify.verify(params, alipayConfig)) {
            sys.out.print("success");
            String trade_status = params.get("trade_status");
            if ("TRADE_SUCCESS".equals(trade_status)) {
                // TODO 更新订单?
            }
        } else {
            sys.out.print("fail");
        }
    }

}
