package org.nutz.walnut.ext.www.bean;

import org.nutz.json.JsonField;
import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.util.Wn;

public class WnCoupon {

    private String id;

    private String title;

    private String owner;

    /**
     * 类型，1:代金券，2:折扣券
     */
    private int type;

    /**
     * 代金券：元；折扣券0.0~1.0浮点
     */
    private float value;

    /**
     * 订单满足这个金额方可应用，0表示不限
     */
    @JsonField("thres")
    private int threshold;

    public boolean isCash() {
        return 1 == this.type;
    }

    public boolean isDiscount() {
        return 2 == this.type;
    }

    public boolean canUse(float totalPrice) {
        if (totalPrice >= threshold) {
            return true;
        }
        return false;
    }

    public float apply(float fee) {
        if (this.isCash()) {
            return Math.max(0, fee - this.value);
        }
        if (this.isDiscount()) {
            return fee * this.value;
        }
        return fee;
    }

    /**
     * 过期时间
     */
    @JsonField("cpn_expi")
    private long expi;

    public void updateBy(NutBean bean) {
        id = bean.getString("id");
        title = bean.getString("title");
        owner = bean.getString("owner");
        type = bean.getInt("type", 1);
        value = bean.getFloat("value", 0.0f);
        threshold = bean.getInt("thres", 0);
        expi = bean.getLong("cpn_expi", 0);
    }

    public WnCoupon clone() {
        WnCoupon pro = new WnCoupon();
        pro.id = this.id;
        pro.title = this.title;
        pro.owner = this.owner;
        pro.type = this.type;
        pro.value = this.value;
        pro.threshold = this.threshold;
        pro.expi = this.expi;
        return pro;
    }

    @Override
    public int hashCode() {
        if (null == id) {
            return 0;
        }
        return id.hashCode();
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

    public boolean isMine(WnAccount user) {
        if (null == owner)
            return true;

        if (null == user)
            return true;

        return user.isSameId(owner);
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public int getThreshold() {
        return threshold;
    }

    public void setThreshold(int thres) {
        this.threshold = thres;
    }

    public boolean isExpired() {
        if (expi > 0) {
            long now = Wn.now();
            return now > expi;
        }
        return false;
    }

    public long getExpi() {
        return expi;
    }

    public void setExpi(long expi) {
        this.expi = expi;
    }

}
