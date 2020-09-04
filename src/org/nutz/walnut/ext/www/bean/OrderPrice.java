package org.nutz.walnut.ext.www.bean;

import org.nutz.lang.Strings;
import org.nutz.walnut.ext.lbs.bean.LbsFreight;

/**
 * 封装一个订单价格相关的细节信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class OrderPrice {

    /**
     * 产品小计
     */
    WnProduct[] products;

    /**
     * 运费部分详情
     */
    LbsFreight freightDetail;

    /**
     * 商品总金额
     */
    float total;

    /**
     * 运费
     */
    float freight;

    /**
     * 优惠金额
     */
    float discount;

    /**
     * 订单总金额（包括运费）
     */
    float price;

    /**
     * 优惠后金额，用来实际支付
     */
    float fee;

    /**
     * 货币单位，默认 RMB
     */
    String currency;

    public WnProduct[] getProducts() {
        return products;
    }

    public void setProducts(WnProduct[] products) {
        this.products = products;
    }

    public LbsFreight getFreightDetail() {
        return freightDetail;
    }

    public void setFreightDetail(LbsFreight freightDetail) {
        this.freightDetail = freightDetail;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public float getFreight() {
        return freight;
    }

    public void setFreight(float freight) {
        this.freight = freight;
    }

    public float getDiscount() {
        return discount;
    }

    public void setDiscount(float discount) {
        this.discount = discount;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public float getFee() {
        return fee;
    }

    public void setFee(float fee) {
        this.fee = fee;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setCurrency(String currency, String dftCurrency) {
        this.currency = Strings.sBlank(currency, dftCurrency);
    }

}
