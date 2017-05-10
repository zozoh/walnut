package org.nutz.walnut.ext.payment;

import org.nutz.walnut.api.io.WnObj;

/**
 * 封装一条支付记录
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnPayObj extends WnObj {

    static final String KEY_BRIEF = "brief";
    static final String KEY_SEND_AT = "send_at";
    static final String KEY_CLOSE_AT = "close_at";
    static final String KEY_SELLER_NM = "seller_nm";
    static final String KEY_SELLER_ID = "seller_id";
    static final String KEY_BUYER_NM = "buyer_nm";
    static final String KEY_BUYER_ID = "buyer_id";
    static final String KEY_BUYER_TP = "buyer_tp";
    static final String KEY_PAY_TP = "pay_tp";
    static final String KEY_CUR = "cur";
    static final String KEY_FEE = "fee";
    static final String KEY_ST = "st";

    WnPay3xStatus status();

    boolean isStatusOk();

    boolean isStatusFail();

    boolean isStatusWait();

    WnPayObj status(WnPay3xStatus status);

    boolean isPayType(String payType);

    boolean isBuyerType(String buyerType);

    boolean isBuyerTypeWalnut();

    boolean hasScript();

    boolean isClosed();

    boolean isSended();

}
