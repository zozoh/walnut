package com.site0.walnut.ext.net.alipay;

/**
 * 
 */

public class AlipayConfig {

    public static final String ALIPAY_GATEWAY_NEW = "https://mapi.alipay.com/gateway.do?";

    // 合作身份者ID，签约账号
    public String partner = "";

    // // 收款支付宝账号
    // public String seller_id = partner;

    // MD5密钥，安全检验码
    public String key = "";

    public String privateKey = "";

    // 签名方式
    public String sign_type = "MD5";

    // 支付类型 ，无需修改
    public String payment_type = "1";
    
    public String pay_notify_url;

    public String pay_return_url;
}
