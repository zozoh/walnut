package org.nutz.walnut.ext.payment.weixin;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.payment.WnPay3x;
import org.nutz.walnut.ext.payment.WnPay3xRe;
import org.nutz.walnut.ext.payment.WnPayObj;

/**
 * 微信主动扫二维码付款
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WxQrcodePay3x extends WnPay3x {

    @Override
    public WnPay3xRe send(WnPayObj po, String... args) {
        throw Lang.noImplement();
    }

    @Override
    public WnPay3xRe check(WnPayObj po) {
        throw Lang.noImplement();
    }

    @Override
    public WnPay3xRe complete(WnPayObj po, NutMap req) {
        throw Lang.noImplement();
    }

}
