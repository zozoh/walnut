package org.nutz.walnut.ext.net.payment.alipay;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.net.payment.WnPay3x;
import org.nutz.walnut.ext.net.payment.WnPay3xRe;
import org.nutz.walnut.ext.net.payment.WnPayObj;

/**
 * 支付宝被物理码枪扫付款码支付
 * <p>
 * !!! 这个先不搞
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class ZfbScanPay3x extends WnPay3x {

    @Override
    public WnPay3xRe send(WnPayObj po, String... args) {
        return null;
    }

    @Override
    public WnPay3xRe check(WnPayObj po) {
        return null;
    }

    @Override
    public WnPay3xRe complete(WnPayObj po, NutMap req) {
        return null;
    }

}
