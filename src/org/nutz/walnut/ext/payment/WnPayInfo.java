package org.nutz.walnut.ext.payment;

import org.nutz.lang.util.NutMap;

/**
 * 用来创建支付单的信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnPayInfo {

    /**
     * 用 `@wn` 表示walnut用户 <br>
     * 否则用一个域名来表示买家所在域
     */
    public String buyer_tp;

    /**
     * 买家ID
     */
    public String buyer_id;

    /**
     * 买家名称
     */
    public String buyer_nm;

    /**
     * 卖家信息（域ID）
     */
    public String seller_id;

    /**
     * 卖家信息（域名）
     */
    public String seller_nm;

    /**
     * 支付的金额，单位是元
     */
    public float fee;

    /**
     * 默认是 RMB，表示货币
     */
    public String cur;

    /**
     * 支付单简要描述
     */
    public String brief;

    /**
     * 更多的自定义支付单元数据
     */
    public NutMap meta;

}
