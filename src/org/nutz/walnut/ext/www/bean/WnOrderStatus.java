package org.nutz.walnut.ext.www.bean;

public enum WnOrderStatus {

    /**
     * 新建
     */
    NW,
    
    /**
     * 等待支付（已经关联了支付单）
     */
    WT,
    
    /**
     * 买家已经支付成功
     */
    OK,
    
    /**
     * 买家已经支付失败
     */
    FA,
    
    /**
     * 卖家已发货
     */
    SD,
    
    /**
     * 订单完成
     */
    DN
}