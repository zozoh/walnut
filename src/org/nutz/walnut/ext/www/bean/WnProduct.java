package org.nutz.walnut.ext.www.bean;

import org.nutz.lang.util.NutBean;

public class WnProduct {

    private String id;

    private String title;

    private float price;

    private int amount;

    public void updateBy(NutBean bean) {
        title = bean.getString("title");
        price = bean.getFloat("price", 0.0f);
        // id/amount 应该不在更新之列
    }

    public WnProduct clone() {
        WnProduct pro = new WnProduct();
        pro.id = this.id;
        pro.title = this.title;
        pro.price = this.price;
        pro.amount = this.amount;
        return pro;
    }

    @Override
    public int hashCode() {
        if (null == id) {
            return 0;
        }
        return id.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s<%s>:%fx%d", title, id, price, amount);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

}
