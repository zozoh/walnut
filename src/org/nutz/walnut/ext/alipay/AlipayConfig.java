package org.nutz.walnut.ext.alipay;

/**
 * 
 */

public class AlipayConfig {

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

}
