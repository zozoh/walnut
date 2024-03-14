package com.site0.walnut.ext.data.www.bean;

import org.nutz.lang.Strings;
import org.nutz.lang.util.IntRegion;

public class PriceRuleItem {

    private String sku;

    private float price;

    private IntRegion skuRegion;

    public boolean isMatch(WnProduct pro) {
        if (null == skuRegion)
            return false;
        int n = pro.getPcount();
        if (n <= 0) {
            n = pro.getAmount();
        }
        return skuRegion.match(n);
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
        if (!Strings.isBlank(sku)) {
            this.skuRegion = IntRegion.Int(sku);
        } else {
            this.skuRegion = null;
        }
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public IntRegion getSkuRegion() {
        return skuRegion;
    }

}
