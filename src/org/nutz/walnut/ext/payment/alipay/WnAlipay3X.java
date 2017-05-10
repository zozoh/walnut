package org.nutz.walnut.ext.payment.alipay;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.ext.payment.WnPay3X;
import org.nutz.walnut.ext.payment.WnPayObj;
import org.nutz.walnut.ext.payment.WnPayRe;

public class WnAlipay3X implements WnPay3X {

    @Override
    public WnPayRe send(WnIo io, WnPayObj po) {
        return null;
    }

    @Override
    public WnPayRe doResult(WnIo io, NutMap req, WnPayObj po) {
        return null;
    }

}
