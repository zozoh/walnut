package org.nutz.walnut.ext.payment;

import org.nutz.lang.util.NutBean;
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
    public boolean isDone() {
        WnPay3xStatus st = this.status();
        return WnPay3xStatus.FAIL == st || WnPay3xStatus.OK == st;
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

    @Override
    public WnPay3xRe getPayReturn() {
        WnPay3xRe re = new WnPay3xRe();
        re.setPayObjId(this.id());
        re.setStatus(this.status());
        re.setData(this.get(KEY_RE_OBJ));
        re.setDataType(this.getEnum(KEY_RE_TP, WnPay3xDataType.class));
        return re;
    }

    @Override
    public NutBean toBean() {
        return this.pickBy("^(id|nm|tp|ct|lm|brief|fee|cur|st|re_.+|pay_.+|buyer_.+|seller_.+|len|send_at|close_at)$");
    }

}
