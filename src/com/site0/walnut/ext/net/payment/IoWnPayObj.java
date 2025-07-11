package com.site0.walnut.ext.net.payment;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import com.site0.walnut.core.bean.WnIoObj;
import com.site0.walnut.login.usr.WnUser;

/**
 * 封装一个支付单的数据
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class IoWnPayObj extends WnIoObj implements WnPayObj {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isStatusOk() {
        return WnPay3xStatus.OK == this.getStatus();
    }

    @Override
    public boolean isStatusFail() {
        return WnPay3xStatus.FAIL == this.getStatus();
    }

    @Override
    public boolean isStatusWait() {
        return WnPay3xStatus.WAIT == this.getStatus();
    }

    @Override
    public boolean isDone() {
        WnPay3xStatus st = this.getStatus();
        return WnPay3xStatus.FAIL == st || WnPay3xStatus.OK == st;
    }

    @Override
    public WnPay3xStatus getStatus() {
        return this.getEnum(WnPays.KEY_ST, WnPay3xStatus.class);
    }

    @Override
    public void setStatus(WnPay3xStatus status) {
        this.put(WnPays.KEY_ST, status);
    }

    @Override
    public boolean isPayType(WnPayType payType) {
        if (null == payType)
            return false;

        return this.isEnum(WnPays.KEY_PAY_TP, payType);
    }

    @Override
    public WnPayType getPayType() {
        return this.getEnum(WnPays.KEY_PAY_TP, WnPayType.class);
    }

    @Override
    public void setPayType(String payType) {
        if (null == payType) {
            this.put(WnPays.KEY_PAY_TP, null);
        } else {
            String pt = payType.replace('.', '_').toUpperCase();
            WnPayType wpt = WnPayType.valueOf(pt);
            this.put(WnPays.KEY_PAY_TP, wpt);
        }
    }

    @Override
    public void setPayType(WnPayType payType) {
        if (null == payType) {
            this.put(WnPays.KEY_PAY_TP, null);
        } else {
            this.put(WnPays.KEY_PAY_TP, payType.name());
        }
    }

    @Override
    public String getBrief(String dftBrief) {
        return this.getString(WnPays.KEY_BRIEF, dftBrief);
    }

    @Override
    public String getBrief() {
        return this.getString(WnPays.KEY_BRIEF);
    }

    @Override
    public void setBrief(String brief) {
        this.put(WnPays.KEY_BRIEF, brief);
    }

    @Override
    public boolean isSended() {
        return this.getLong(WnPays.KEY_SEND_AT) > 0;
    }

    @Override
    public long getSendAt() {
        return this.getLong(WnPays.KEY_SEND_AT);
    }

    @Override
    public void setSendAt(long sendAt) {
        this.put(WnPays.KEY_SEND_AT, sendAt);
    }

    @Override
    public boolean isApplied() {
        return this.getLong(WnPays.KEY_APPLY_AT) > 0;
    }

    @Override
    public long getApplyAt() {
        return this.getLong(WnPays.KEY_APPLY_AT);
    }

    @Override
    public void setApplyAt(long applyAt) {
        this.put(WnPays.KEY_APPLY_AT, applyAt);
    }

    @Override
    public boolean isClosed() {
        return this.getLong(WnPays.KEY_CLOSE_AT) > 0;
    }

    @Override
    public long getCloseAt() {
        return this.getLong(WnPays.KEY_CLOSE_AT);
    }

    @Override
    public void setCloseAt(long closeAt) {
        this.put(WnPays.KEY_CLOSE_AT, closeAt);
    }

    @Override
    public String getSellerId() {
        return this.getString(WnPays.KEY_SELLER_ID);
    }

    @Override
    public void setSellerId(String sellerId) {
        this.put(WnPays.KEY_SELLER_ID, sellerId);
    }

    @Override
    public String getSellerName() {
        return this.getString(WnPays.KEY_SELLER_NM);
    }

    @Override
    public void setSellerName(String sellerName) {
        this.put(WnPays.KEY_SELLER_NM, sellerName);
    }

    @Override
    public boolean isWalnutBuyer() {
        return Strings.isBlank(this.getBuyerType());
    }

    @Override
    public boolean isDomainBuyer() {
        return !Strings.isBlank(this.getBuyerType());
    }

    @Override
    public String getBuyerId() {
        return this.getString(WnPays.KEY_BUYER_ID);
    }

    @Override
    public void setBuyerId(String buyerId) {
        this.put(WnPays.KEY_BUYER_ID, buyerId);
    }

    @Override
    public String getBuyerName() {
        return this.getString(WnPays.KEY_BUYER_NM);
    }

    @Override
    public void setBuyerName(String buyerName) {
        this.put(WnPays.KEY_BUYER_NM, buyerName);
    }

    @Override
    public String getBuyerType() {
        return this.getString(WnPays.KEY_BUYER_TP);
    }

    @Override
    public void setBuyerType(String buyerType) {
        this.put(WnPays.KEY_BUYER_TP, buyerType);
    }

    @Override
    public String getPayTarget() {
        return this.getString(WnPays.KEY_PAY_TARGET);
    }

    @Override
    public void setPayTarget(String payTarget) {
        this.put(WnPays.KEY_PAY_TARGET, payTarget);
    }

    @Override
    public String getCurrency() {
        return this.getString(WnPays.KEY_CUR);
    }

    @Override
    public void setCurrency(String cur) {
        this.put(WnPays.KEY_CUR, cur);
    }

    @Override
    public int getPrice() {
        return this.getInt(WnPays.KEY_PRICE);
    }

    @Override
    public void setPrice(int price) {
        this.put(WnPays.KEY_PRICE, price);
    }

    @Override
    public int getFee() {
        return this.getInt(WnPays.KEY_FEE);
    }

    @Override
    public float getFeeInYuan() {
        return ((float) this.getFee()) / 100.0f;
    }

    @Override
    public void setFee(int fee) {
        this.put(WnPays.KEY_FEE, fee);
    }

    @Override
    public String getReturnUrl() {
        return this.getString(WnPays.KEY_RETURN_URL);
    }

    @Override
    public void setReturnUrl(String returnUrl) {
        this.put(WnPays.KEY_RETURN_URL, returnUrl);
    }

    @Override
    public WnPay3xDataType getReturnType() {
        return this.getEnum(WnPays.KEY_RE_TP, WnPay3xDataType.class);
    }

    @Override
    public void setReturnType(WnPay3xDataType reType) {
        if (null == reType) {
            this.put(WnPays.KEY_RE_TP, null);
        } else {
            this.put(WnPays.KEY_RE_TP, reType.name());
        }
    }

    @Override
    public Object getReturnData() {
        return this.get(WnPays.KEY_RE_OBJ);
    }

    @Override
    public void setReturnData(Object reData) {
        this.put(WnPays.KEY_RE_OBJ, reData);
    }

    @Override
    public String getClientIP() {
        return this.getString(WnPays.KEY_CLIENT_IP);
    }

    @Override
    public void setClientIP(String clientIP) {
        this.put(WnPays.KEY_CLIENT_IP, clientIP);
    }

    @Override
    public boolean hasScript() {
        return this.len() > 0;
    }

    @Override
    public boolean hasPayTarget() {
        return this.has(WnPays.KEY_PAY_TARGET);
    }

    @Override
    public boolean isTheSeller(WnUser u) {
        if (null == u)
            return false;
        return u.isSameId(this.getString(WnPays.KEY_SELLER_ID));
    }

    @Override
    public boolean isTheBuyer(WnUser u) {
        return u.isSameId(this.getString(WnPays.KEY_BUYER_ID));
    }

    @Override
    public WnPay3xRe getPayReturn() {
        WnPay3xRe re = new WnPay3xRe();
        re.setPayObjId(this.id());
        re.setStatus(this.getStatus());
        re.setDataType(this.getReturnType());
        re.setData(this.getReturnData());
        return re;
    }

    @Override
    public NutBean toBean() {
        return this.pickBy("^(id|nm|tp|ct|lm|brief|fee|cur|st|re_.+|pay_.+|buyer_.+|seller_.+|len|send_at|close_at)$");
    }

}
