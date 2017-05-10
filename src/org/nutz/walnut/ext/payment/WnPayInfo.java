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
    public String buyerType;

    /**
     * 买家ID
     */
    public String buyerId;

    /**
     * 买家名称
     */
    public String buyterName;

    /**
     * 卖家信息（域ID）
     */
    public String sellerId;

    /**
     * 卖家信息（域名）
     */
    public String sellerName;

    /**
     * 支付的金额，单位是元
     */
    public float fee;

    /**
     * 默认是 RMB，表示货币
     */
    public String currency;

    /**
     * 支付单简要描述
     */
    public String brief;

    /**
     * 更多的自定义支付单元数据
     */
    private NutMap meta;

    public boolean hasMeta() {
        return null != meta && meta.size() > 0;
    }

    public NutMap meta() {
        if (null == meta) {
            synchronized (this) {
                if (null == meta) {
                    meta = new NutMap();
                }
            }
        }
        return meta;
    }

}
