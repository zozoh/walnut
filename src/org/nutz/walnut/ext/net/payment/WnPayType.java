package org.nutz.walnut.ext.net.payment;

/**
 * 支付类型
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public enum WnPayType {

    /**
     * 微信主动扫二维码付款
     */
    WX_QRCODE,
    
    /**
     * 微信公众号内支付 
     */
    WX_JSAPI,
    
    /**
     * 微信被物理码枪扫付款码支付
     */
    WX_SCAN,
    
    /**
     * 支付宝主动扫二维码付款
     */
    ZFB_QRCODE,
    
    /**
     * 支付宝被物理码枪扫付款码支付
     */
    ZFB_SCAN,
    
    /**
     * Paypal支付
     */
    PAYPAL,
    
    /**
     * 免费
     */
    FREE
}
