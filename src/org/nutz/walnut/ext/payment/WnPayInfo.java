package org.nutz.walnut.ext.payment;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.api.usr.WnUsrService;
import org.nutz.walnut.util.Wn;

/**
 * 用来创建支付单的信息
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public class WnPayInfo {

    /**
     * 用户类型
     * 
     * <ul>
     * <li>1 - walnut 用户 (BUYTER_WN)
     * <li>2 - 属于卖家域的某用户 (BUYTER_DUSR)
     * </ul>
     * 
     * @see WnPayObj#BUYTER_WN
     * @see WnPayObj#BUYTER_DUSR
     */
    public int buyer_tp;

    public boolean hasBuyterType() {
        return buyer_tp > 0;
    }

    public boolean isWnUsr() {
        return WnPayObj.BUYTER_WN == buyer_tp;
    }

    public boolean isDUsr() {
        return WnPayObj.BUYTER_DUSR == buyer_tp;
    }

    public WnPayInfo asWnUsr() {
        buyer_tp = WnPayObj.BUYTER_WN;
        return this;
    }

    public WnPayInfo asDusr() {
        buyer_tp = WnPayObj.BUYTER_DUSR;
        return this;
    }

    /**
     * 买家ID
     */
    public String buyer_id;

    /**
     * 买家名称
     */
    public String buyer_nm;

    /**
     * 检查是否信息完毕
     */
    public void assertBuyerPerfect() {
        if (Strings.isBlank(buyer_id) || Strings.isBlank(buyer_nm)) {
            throw Er.createf("e.pay.buyer.imperfect", "id:<%s> / nm:<%s>", buyer_id, buyer_nm);
        }
    }

    /**
     * 卖家信息（域ID）
     */
    public String seller_id;

    /**
     * 卖家信息（域名）
     */
    public String seller_nm;

    public WnUsr checkSeller(WnUsrService usrs, WnUsr me) {
        boolean noSeId = Strings.isBlank(seller_id);
        boolean noSeNm = Strings.isBlank(seller_nm);

        // 补足
        if (noSeId || noSeNm) {
            // 采用默认用户
            if (noSeId && noSeNm) {
                seller_id = me.id();
                seller_nm = me.name();
                return me;
            }
            // 补足 ID
            else if (noSeId) {
                WnUsr u = usrs.check(seller_nm);
                seller_id = u.id();
                seller_nm = u.name();
                return u;
            }
            // 补足名称
            else {
                WnUsr u = usrs.check("id:" + seller_id);
                seller_nm = u.name();
                return u;
            }
        }
        // 如果两个都有，则比较一下是否匹配
        else {
            WnUsr u = usrs.check(seller_nm);
            if (!u.isSameId(seller_id)) {
                throw Er.create("e.pay.noMatchSeller", seller_id + " not " + seller_nm);
            }
            return u;
        }
    }

    /**
     * 支付的金额，单位是分
     */
    public int fee;

    /**
     * 默认是 RMB，表示货币
     */
    public String cur;

    /**
     * 支付单简要描述
     */
    public String brief;

    public void checkBrief() {
        if (Strings.isBlank(brief)) {
            brief = String.format("Pay to %s", seller_nm);
        }
    }

    /**
     * 更多的自定义支付单元数据
     */
    public NutMap meta;

    /**
     * 回调脚本名称
     */
    public String callbackName;

    public boolean hasCallback() {
        return !Strings.isBlank(callbackName);
    }

    public String readCallback(WnIo io, WnUsr seller) {
        WnObj oCallback = io.check(null,
                                   Wn.appendPath(seller.home(), ".payment/callback", callbackName));
        return io.readText(oCallback);
    }

}