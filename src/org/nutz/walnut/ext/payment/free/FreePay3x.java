package org.nutz.walnut.ext.payment.free;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.payment.WnPay3x;
import org.nutz.walnut.ext.payment.WnPay3xDataType;
import org.nutz.walnut.ext.payment.WnPay3xRe;
import org.nutz.walnut.ext.payment.WnPay3xStatus;
import org.nutz.walnut.ext.payment.WnPayObj;

public class FreePay3x extends WnPay3x {

    @Override
    public WnPay3xRe send(WnPayObj po, String... args) {
        WnPay3xRe re = new WnPay3xRe();
        re.setStatus(WnPay3xStatus.OK);
        re.setDataType(WnPay3xDataType.TEXT);
        re.setData("Done for pay");
        return re;
    }

    @Override
    public WnPay3xRe check(WnPayObj po) {
        WnPay3xRe re = new WnPay3xRe();
        re.setStatus(WnPay3xStatus.OK);
        re.setDataType(WnPay3xDataType.TEXT);
        re.setData("Done for pay");
        return re;
    }

    @Override
    public WnPay3xRe complete(WnPayObj po, NutMap req) {
        WnPay3xRe re = new WnPay3xRe();
        re.setStatus(WnPay3xStatus.OK);
        re.setDataType(WnPay3xDataType.TEXT);
        re.setData("Done for pay");
        return re;
    }

}
