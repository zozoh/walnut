package org.nutz.walnut.ext.www.bean;

import org.nutz.json.Json;
import org.nutz.json.JsonField;
import org.nutz.json.JsonFormat;
import org.nutz.lang.Strings;
import org.nutz.lang.util.NutBean;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.payment.WnPay3xRe;

public class WnOrder {

    private String id;

    private WnProduct[] products;

    private WnCoupon[] coupons;

    private String title;

    /**
     * 买家名称（支付配置目录名）
     */
    private String seller;

    /**
     * 账户库的ID
     */
    private String accounts;

    @JsonField("buyer_id")
    private String buyerId;

    private float price;

    private float fee;

    @JsonField("pay_tp")
    private String payType;

    @JsonField("pay_id")
    private String payId;

    @JsonField("pay_re")
    private WnPay3xRe payReturn;

    @JsonField("st")
    private WnOrderStatus status;

    @JsonField("ct")
    private long createTime;

    @JsonField("lm")
    private long lastModified;

    @JsonField("wt_at")
    private long waitAt;

    @JsonField("ok_at")
    private long okAt;

    @JsonField("fa_at")
    private long failAt;

    @JsonField("sp_at")
    private long shipAt;

    @JsonField("dn_at")
    private long doneAt;

    @Override
    public WnOrder clone() {
        return this.duplicate(true);
    }

    /**
     * @param or
     *            拷贝的目标，会将自身的内容复制过去
     * @param fullCopy
     *            true 表示完整复制。false 表示复制主要内容以便以此为模板
     * @return 新的实例
     */
    public void copyTo(WnOrder or, boolean fullCopy) {
        // 产品
        if (null != this.products) {
            or.products = new WnProduct[this.products.length];
            for (int i = 0; i < this.products.length; i++) {
                WnProduct pro = this.products[i];
                or.products[i] = pro.clone();
            }
        }
        // 优惠券
        if (null != this.coupons) {
            or.coupons = new WnCoupon[this.coupons.length];
            for (int i = 0; i < this.coupons.length; i++) {
                WnCoupon cou = this.coupons[i];
                or.coupons[i] = cou.clone();
            }
        }
        // 其他关键字段
        or.title = this.title;
        or.seller = this.seller;
        or.buyerId = this.buyerId;
        or.price = this.price;
        or.fee = this.fee;
        or.payType = this.payType;

        // 其他字段
        if (fullCopy) {
            or.id = this.id;
            or.status = this.status;
            or.createTime = this.createTime;
            or.lastModified = this.lastModified;
            or.payId = this.payId;
            or.waitAt = this.waitAt;
            or.okAt = this.okAt;
            or.failAt = this.failAt;
            or.shipAt = this.shipAt;
            or.doneAt = this.doneAt;
        }
    }

    /**
     * @param fullCopy
     *            true 表示完整复制。false 表示复制主要内容以便以此为模板
     * @return 新的实例
     */
    public WnOrder duplicate(boolean fullCopy) {
        WnOrder or = new WnOrder();
        this.copyTo(or, fullCopy);
        return or;
    }

    public void updateBy(NutBean bean) {
        String json = Json.toJson(bean);
        WnOrder or = Json.fromJson(WnOrder.class, json);
        or.copyTo(this, true);
    }

    public void setTo(NutBean bean, String actived, String locked) {
        NutMap meta = this.toMeta(actived, locked);
        bean.putAll(meta);
    }

    public NutMap toMeta() {
        return this.toMeta(null, null);
    }

    public NutMap toMeta(String actived, String locked) {
        JsonFormat jfmt = JsonFormat.compact();
        if (!Strings.isBlank(locked))
            jfmt.setLocked(locked);
        if (!Strings.isBlank(actived)) {
            jfmt.setActived(actived);
        }
        String json = Json.toJson(this, jfmt);
        return Json.fromJson(NutMap.class, json);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean hasProducts() {
        return null != products && products.length > 0;
    }

    public WnProduct[] getProducts() {
        return products;
    }

    public void setProducts(WnProduct[] products) {
        this.products = products;
    }

    public boolean hasCoupons() {
        return null != coupons && coupons.length > 0;
    }

    public WnCoupon[] getCoupons() {
        return coupons;
    }

    public void setCoupons(WnCoupon[] coupons) {
        this.coupons = coupons;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getAccounts() {
        return accounts;
    }

    public void setAccounts(String accounts) {
        this.accounts = accounts;
    }

    public String getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(String buyerId) {
        this.buyerId = buyerId;
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

    public String getPayTypePrefix() {
        if (null != payType) {
            int pos = payType.indexOf('.');
            if (pos >= 0) {
                return payType.substring(0, pos);
            }
            return payType;
        }
        return null;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public boolean hasPayId() {
        return !Strings.isBlank(payId);
    }

    public String getPayId() {
        return payId;
    }

    public void setPayId(String payId) {
        this.payId = payId;
    }

    public WnPay3xRe getPayReturn() {
        return payReturn;
    }

    public void setPayReturn(WnPay3xRe payReturn) {
        this.payReturn = payReturn;
    }

    public WnOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WnOrderStatus status) {
        this.status = status;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getLastModified() {
        return lastModified;
    }

    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    public long getWaitAt() {
        return waitAt;
    }

    public void setWaitAt(long waitAt) {
        this.waitAt = waitAt;
    }

    public long getOkAt() {
        return okAt;
    }

    public void setOkAt(long okAt) {
        this.okAt = okAt;
    }

    public long getFailAt() {
        return failAt;
    }

    public void setFailAt(long failAt) {
        this.failAt = failAt;
    }

    public long getShipAt() {
        return shipAt;
    }

    public void setShipAt(long shipAt) {
        this.shipAt = shipAt;
    }

    public long getDoneAt() {
        return doneAt;
    }

    public void setDoneAt(long doneAt) {
        this.doneAt = doneAt;
    }

}
