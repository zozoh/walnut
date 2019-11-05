package org.nutz.walnut.ext.www.bean;

import org.nutz.walnut.api.io.WnObj;

public class WnOrderInfo {

    private String payType;

    private String[] products;

    private String[] coupons;

    private WnObj buyer;

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String[] getProducts() {
        return products;
    }

    public void setProducts(String[] products) {
        this.products = products;
    }

    public String[] getCoupons() {
        return coupons;
    }

    public void setCoupons(String[] coupons) {
        this.coupons = coupons;
    }

    public WnObj getBuyer() {
        return buyer;
    }

    public void setBuyer(WnObj buyer) {
        this.buyer = buyer;
    }

}
