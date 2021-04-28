package org.nutz.walnut.ext.net.payment.weixin;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.net.payment.WnPay3xRe;
import org.nutz.walnut.ext.net.payment.WnPayObj;

/**
 * 微信公众号内支付
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WxJsApiPay3x extends AbstractWeixinPay3x {

    @Override
    public WnPay3xRe send(WnPayObj po, String... args) {
        po.put("wx_trade_type", "JSAPI");
        return this.unifiedorder(po, po.getString("wx_openid"));
    }

    @Override
    public WnPay3xRe complete(WnPayObj po, NutMap req) {
        po.put("wx_trade_type", "JSAPI");
        return this.notify_result(po, req);
    }

}
