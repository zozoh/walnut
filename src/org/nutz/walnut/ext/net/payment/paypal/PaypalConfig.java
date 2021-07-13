package org.nutz.walnut.ext.net.payment.paypal;

public class PaypalConfig {

    public String id;
    public String secret;
    /**
     * 支付模式
     * <ul>
     * <li><code>live</code>: 生产环境
     * <li><code>sandbox</code> : 沙盒模式
     * </ul>
     */
    public String mode;
    public String cancelUrl;
    public String returnUrl;
}
