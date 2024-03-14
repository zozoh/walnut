package com.site0.walnut.ext.data.www.bean;

import org.nutz.json.JsonField;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnObj;

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
     * 商品分类名称
     */
    private String cate;

    /**
     * 产品标称单格（原始零售价）
     */
    private float retail;

    /**
     * 产品动态单价，优先应用规则的零售价
     * <p>
     * 如果声明了 proId+priceBy，先应用规则，获取的单价<br>
     * 否则与零售价相同
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
     * 如果设置了价格体系，那么在相同价格体系下， 所有商品购买的总数量，将会影响价格。
     * <p>
     * 这里则是记录一个归纳值，以便根据规则， 从价格规则表中甄选价格
     */
    private int pcount;

    /**
     * 小计： <code>price * amount</code>
     */
    private float subtotal;

    /**
     * 小计： <code>retail * amount</code>
     */
    private float subretail;

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
        cate = bean.getString("cate");
        price = bean.getFloat("price", -1);
        retail = bean.getFloat("retail", -1);

        // price || retail 这俩货有一个声明就成
        if (price < 0) {
            price = retail;
        }
        if (retail < 0) {
            retail = price;
        }
        // 咋地也不能是负数
        price = Math.max(0, price);
        retail = Math.max(0, retail);

        weight = bean.getFloat("weight", 0);
        freight = bean.getFloat("freight", 0);
        proId = bean.getString("pro_id");
        priceBy = bean.getString("price_by");
        // id/amount/subtotal 应该不在更新之列
    }

    public NutBean toBean() {
        NutMap bean = new NutMap();
        bean.put("id", this.id);
        bean.put("title", this.title);
        bean.put("cate", this.cate);
        bean.put("retail", this.retail);
        bean.put("price", this.price);
        bean.put("weight", this.weight);
        bean.put("freight", this.freight);
        bean.put("amount", this.amount);
        bean.put("subtotal", this.subtotal);
        bean.put("pro_id", this.proId);
        bean.put("price_by", this.priceBy);
        return bean;
    }

    public WnProduct clone() {
        WnProduct pro = new WnProduct();
        pro.id = this.id;
        pro.title = this.title;
        pro.cate = this.cate;
        pro.retail = this.retail;
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

    public String getCate() {
        return cate;
    }

    public void setCate(String cate) {
        this.cate = cate;
    }

    public float getRetail() {
        return retail;
    }

    public void setRetail(float retail) {
        this.retail = retail;
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

    public float getSubretail() {
        return subretail;
    }

    public void setSubretail(float subretail) {
        this.subretail = subretail;
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
