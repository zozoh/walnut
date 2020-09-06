package org.nutz.walnut.ext.www.bean;

import org.nutz.json.JsonField;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.io.WnObj;

public class WnProduct {

    /**
     * 商品 ID
     */
    private String id;

    /**
     * 显示标题
     */
    private String title;

    /**
     * 产品单价
     * <p>
     * 如果声明了 proId+priceBy，则优先应用规则，并更新本字段
     */
    private float price;

    /**
     * 重量（公斤）（可作为运费计算依据）
     * <p>
     * 0或者负数表示不参与运费计算
     */
    private float weight;

    /**
     * 固定运费（元）
     * <p>
     * 表示特殊商品，运费一律固定一个价格<br>
     * 如果未设定或为0或负数，则表示需要计算运费
     */
    private float freight;

    /**
     * 购买数量
     */
    private int amount;

    /**
     * 如果设置了价格体系，那么在相同价格体系下，所有商品购买的总数量
     */
    private int pcount;

    /**
     * 小计： <code>price * amount</code>
     */
    private float subtotal;

    /**
     * 价格规则对象的 ID
     */
    @JsonField("pro_id")
    private String proId;

    /**
     * 价格规则名称
     */
    @JsonField("price_by")
    private String priceBy;

    @JsonField(ignore = true)
    WnObj obj;

    public void updateBy(NutBean bean) {
        title = bean.getString("title");
        price = bean.getFloat("price", 0);
        weight = bean.getFloat("weight", 0);
        freight = bean.getFloat("freight", 0);
        proId = bean.getString("pro_id");
        priceBy = bean.getString("price_by");
        // id/amount/subtotal 应该不在更新之列
    }

    public WnProduct clone() {
        WnProduct pro = new WnProduct();
        pro.id = this.id;
        pro.title = this.title;
        pro.price = this.price;
        pro.weight = this.weight;
        pro.freight = this.freight;
        pro.amount = this.amount;
        pro.subtotal = this.subtotal;
        pro.proId = this.proId;
        pro.priceBy = this.priceBy;
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
        return String.format("%s<%s>:%sx%d=%s", title, id, price, amount, subtotal);
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

    public boolean hasWeight() {
        return weight > 0;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public boolean hasFreight() {
        return freight > 0;
    }

    public float getFreight() {
        return freight;
    }

    public void setFreight(float freight) {
        this.freight = freight;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public int getPcount() {
        return pcount;
    }

    public void setPcount(int pcount) {
        this.pcount = pcount;
    }

    public String getPcountKey() {
        return String.format("%s_%s", proId, priceBy);
    }

    public float getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(float subtotal) {
        this.subtotal = subtotal;
    }

    public boolean hasProId() {
        return !Strings.isBlank(proId);
    }

    public String getProId() {
        return proId;
    }

    public void setProId(String proId) {
        this.proId = proId;
    }

    public boolean hasPriceBy() {
        return !Strings.isBlank(priceBy);
    }

    public String getPriceBy() {
        return priceBy;
    }

    public void setPriceBy(String priceBy) {
        this.priceBy = priceBy;
    }

    public WnObj getObj() {
        return obj;
    }

    public void setObj(WnObj obj) {
        this.obj = obj;
    }

}
