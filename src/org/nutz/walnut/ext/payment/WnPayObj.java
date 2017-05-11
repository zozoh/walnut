package org.nutz.walnut.ext.payment;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;

/**
 * 封装一条支付记录
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public interface WnPayObj extends WnObj {
    // ..............................................................
    static final String KEY_BRIEF = "brief";
    static final String KEY_SEND_AT = "send_at";
    static final String KEY_CLOSE_AT = "close_at";
    static final String KEY_SELLER_NM = "seller_nm";
    static final String KEY_SELLER_ID = "seller_id";
    static final String KEY_BUYER_NM = "buyer_nm";
    static final String KEY_BUYER_ID = "buyer_id";
    static final String KEY_BUYER_TP = "buyer_tp";
    static final String KEY_PAY_TP = "pay_tp";
    static final String KEY_PAY_TARGET = "pay_target";
    static final String KEY_CUR = "cur";
    static final String KEY_FEE = "fee";
    static final String KEY_ST = "st"; // @see WnPay3xStatus
    // ..............................................................
    /**
     * "buyer_tp" 段值（KEY_BUYER_TP）：表示 Walnut 用户，即系统用户
     */
    static final int BUYTER_WN = 1;

    /**
     * "buyer_tp" 段值（KEY_BUYER_TP）：表示 Dusr 用户，即某个域内部的用户
     */
    static final int BUYTER_DUSR = 2;
    // ..............................................................
    /**
     * "pay_tp" 段值（KEY_PAY_TP）：微信主动扫码付款
     */
    static final String PT_WX_QRCODE = "wx.qrcode";
    /**
     * "pay_tp" 段值（KEY_PAY_TP）：微信公众号内支付
     */
    static final String PT_WX_JSAPI = "wx.jsapi";
    /**
     * "pay_tp" 段值（KEY_PAY_TP）：微信被物理码枪扫付款码支付
     */
    static final String PT_WX_SCAN = "wx.scan";
    /**
     * "pay_tp" 段值（KEY_PAY_TP）：支付宝主动扫码付款
     */
    static final String PT_ZFB_QRCODE = "zfb.qrcode";

    // ..............................................................
    
    WnPay3xStatus status();

    boolean isStatusOk();

    boolean isStatusFail();

    boolean isStatusWait();

    WnPayObj status(WnPay3xStatus status);

    boolean isPayType(String payType);

    boolean isBuyerWn();

    boolean isBuyerDusr();

    boolean hasScript();
    
    boolean hasPayTarget();

    boolean isClosed();

    boolean isSended();

    boolean isTheSeller(WnUsr u);

    boolean isTheBuyer(WnUsr u);

}
