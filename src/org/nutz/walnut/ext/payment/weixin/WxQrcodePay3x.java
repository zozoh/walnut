package org.nutz.walnut.ext.payment.weixin;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.payment.WnPay3x;
import org.nutz.walnut.ext.payment.WnPay3xRe;
import org.nutz.walnut.ext.payment.WnPayObj;

public class WxQrcodePay3x implements WnPay3x {

    @Override
    public WnPay3xRe send(WnPayObj po) {
        return null;
    }

    @Override
    public WnPay3xRe check(WnPayObj po) {
        return null;
    }

    @Override
    public WnPay3xRe complete(NutMap req, WnPayObj po) {
        return null;
    }

}
