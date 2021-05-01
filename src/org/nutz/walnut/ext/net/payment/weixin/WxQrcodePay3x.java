package org.nutz.walnut.ext.net.payment.weixin;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.net.payment.WnPay3xRe;
import org.nutz.walnut.ext.net.payment.WnPayObj;

/**
 * 微信主动扫二维码付款
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WxQrcodePay3x extends AbstractWeixinPay3x {

    @Override
    public WnPay3xRe send(WnPayObj po, String... args) {
        return this.unifiedorder(po, null);
    }

    @Override
    public WnPay3xRe complete(WnPayObj po, NutMap req) {
        return this.notify_result(po, req);
    }

}
