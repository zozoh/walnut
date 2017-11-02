package org.nutz.walnut.ext.voucher.hdl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.WnSystem;

public class voucher_xxx_coupon {

    protected NutMap acountCoupon(WnSystem sys, WnObj wobj, int price) {
        int voucher_condition = wobj.getInt("voucher_condition");
        double voucher_discount = wobj.getDouble("voucher_discount");
        NutMap re = new NutMap();
        re.put("price", price);
        int price_new = -1;
        if (voucher_condition > 0 && price < voucher_condition) {
            // 未满足最低金额
            re.put("ok", false);
            re.put("err", "e.cmd.voucher_test_coupon.not_reach_condition");
            re.put("condition", voucher_condition);
        }
        // 满减
        else if (voucher_discount > 1) {
            // 减完之后,起码得有一分钱
            price_new = (int)(price - voucher_discount > 0 ? price - voucher_discount : 1);
        } 
        // 打折
        else {
            // 打折后,起码得有一分钱
            price_new = (price * voucher_discount) > 0 ? ((int)(price * voucher_discount)) : 1;
        }
        if (price_new == -1) {
            // nop
        }
        else if (price_new == price) {
            re.put("ok", false);
            re.put("err", "e.cmd.voucher_test_coupon.no_need");
        }
        else {
            re.put("ok", true);
            re.put("price_new", price_new);
        }
        return re;
    }
}
