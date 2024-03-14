package com.site0.walnut.ext.net.payment.free;

import org.nutz.lang.util.NutMap;
import com.site0.walnut.ext.net.payment.WnPay3x;
import com.site0.walnut.ext.net.payment.WnPay3xDataType;
import com.site0.walnut.ext.net.payment.WnPay3xRe;
import com.site0.walnut.ext.net.payment.WnPay3xStatus;
import com.site0.walnut.ext.net.payment.WnPayObj;

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
