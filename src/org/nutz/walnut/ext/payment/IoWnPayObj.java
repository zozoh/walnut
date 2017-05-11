package org.nutz.walnut.ext.payment;

import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.io.WnBean;

/**
 * 封装一个支付单的数据
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class IoWnPayObj extends WnBean implements WnPayObj {

    @Override
    public boolean isStatusOk() {
        return WnPay3xStatus.OK == this.status();
    }

    @Override
    public boolean isStatusFail() {
        return WnPay3xStatus.FAIL == this.status();
    }

    @Override
    public boolean isStatusWait() {
        return WnPay3xStatus.WAIT == this.status();
    }

    @Override
    public WnPay3xStatus status() {
        return this.getEnum(KEY_ST, WnPay3xStatus.class);
    }

    @Override
    public WnPayObj status(WnPay3xStatus status) {
        this.put(KEY_ST, status);
        return this;
    }

    @Override
    public boolean isPayType(String payType) {
        if (null == payType)
            return false;

        String pt = this.getString(KEY_PAY_TP);
        if (null == pt)
            return false;

        return payType.equals(pt);
    }

    @Override
    public boolean isBuyerWn() {
        return BUYTER_WN == this.getInt(KEY_BUYER_TP, -1);
    }

    @Override
    public boolean isBuyerDusr() {
        return BUYTER_DUSR == this.getInt(KEY_BUYER_TP, -1);
    }

    @Override
    public boolean hasScript() {
        return this.len() > 0;
    }

    @Override
    public boolean hasPayTarget() {
        return this.has(WnPayObj.KEY_PAY_TARGET);
    }

    @Override
    public boolean isClosed() {
        return this.getLong(KEY_CLOSE_AT) > 0;
    }

    @Override
    public boolean isSended() {
        return this.getLong(KEY_SEND_AT) > 0;
    }

    @Override
    public boolean isTheSeller(WnUsr u) {
        return u.isSameId(this.getString(KEY_SELLER_ID));
    }

    @Override
    public boolean isTheBuyer(WnUsr u) {
        return u.isSameId(this.getString(KEY_BUYER_ID));
    }

}
