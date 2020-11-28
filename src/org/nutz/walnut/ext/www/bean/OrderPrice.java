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
     * 总运费
     */
    private float freight;

    /**
     * 商品总价
     */
    private float total;

    /**
     * 标称总价
     */
    private float nominal;

    /**
     * 收益金额
     */
    private float profit;

    /**
     * 基础金额
     */
    private float prefee;

    /**
     * 优惠金额 (基于 prefee 应用优惠券节省的金额)
     */
    private float discount;

    /**
     * 惠前金额 (prefee + freight)
     */
    private float price;

    /**
     * 支付金额(price - discount)
     */
    private float fee;

    /**
     * 货币结算单位，默认 <code>RMB</code>
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

    public float getFreight() {
        return freight;
    }

    public void setFreight(float freight) {
        this.freight = freight;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    public float getNominal() {
        return nominal;
    }

    public void setNominal(float nominal) {
        this.nominal = nominal;
    }

    public float getProfit() {
        return profit;
    }

    public void setProfit(float profit) {
        this.profit = profit;
    }

    public float getPrefee() {
        return prefee;
    }

    public void setPrefee(float prefee) {
        this.prefee = prefee;
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
