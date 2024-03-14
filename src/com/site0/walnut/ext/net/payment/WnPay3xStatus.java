package com.site0.walnut.ext.net.payment;

public enum WnPay3xStatus {

    /**
     * 新创建的，未向第三方平台提交的支付单
     */
    NEW,

    /**
     * 支付成功的支付单
     */
    OK,

    /**
     * 支付失败的支付单
     */
    FAIL,

    /**
     * 等待用户确认的支付单
     */
    WAIT

}
