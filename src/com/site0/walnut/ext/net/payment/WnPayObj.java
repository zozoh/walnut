package com.site0.walnut.ext.net.payment;

import org.nutz.lang.util.NutBean;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.login.usr.WnUser;

/**
 * 封装一条支付记录
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnPayObj extends WnObj {

    boolean hasScript();

    String getBrief(String dftBrief);

    String getBrief();

    void setBrief(String brief);

    boolean isSended();

    long getSendAt();

    void setSendAt(long sendAt);

    boolean isApplied();

    long getApplyAt();

    void setApplyAt(long applyAt);

    boolean isClosed();

    long getCloseAt();

    void setCloseAt(long closeAt);

    boolean isTheSeller(WnUser seller);

    String getSellerId();

    void setSellerId(String sellerId);

    String getSellerName();

    void setSellerName(String sellerName);

    boolean isTheBuyer(WnUser seller);

    boolean isWalnutBuyer();

    boolean isDomainBuyer();

    String getBuyerId();

    void setBuyerId(String buyerId);

    String getBuyerName();

    void setBuyerName(String buyerName);

    String getBuyerType();

    void setBuyerType(String buyerType);

    boolean isPayType(WnPayType payType);

    WnPayType getPayType();

    void setPayType(WnPayType payType);

    void setPayType(String payType);

    boolean hasPayTarget();

    String getPayTarget();

    void setPayTarget(String payTarget);

    String getCurrency();

    void setCurrency(String cur);

    int getPrice();

    void setPrice(int price);

    int getFee();

    float getFeeInYuan();

    void setFee(int fee);

    boolean isStatusOk();

    boolean isStatusFail();

    boolean isStatusWait();

    WnPay3xStatus getStatus();

    void setStatus(WnPay3xStatus status);

    boolean isDone();

    String getReturnUrl();

    void setReturnUrl(String returnUrl);

    WnPay3xDataType getReturnType();

    void setReturnType(WnPay3xDataType reType);

    Object getReturnData();

    void setReturnData(Object reData);

    WnPay3xRe getPayReturn();

    String getClientIP();

    void setClientIP(String clientIP);

    NutBean toBean();

}
