package org.nutz.walnut.ext.payment;

public enum WnPay3xDataType {

    /**
     * data 段是一个第三方付款页面的 URL
     */
    LINK,

    /**
     * data 段是一个第三方付款页面的 URL，可以用 iframe 来嵌套显示
     * <p>
     * <em>其实呢，这个专门是给支付宝这个二货设计的返回类型 -_-!</em>
     */
    IFRAME,

    /**
     * data 段是一个第三方付款二维码的文字形式
     */
    QRCODE,

    /**
     * data 段是Map对象，表示一段付款相关配置信息
     */
    JSON,

    /**
     * data 段是纯文本
     */
    TEXT
}
