package org.nutz.walnut.ext.voucher;

import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;

/**
 * 优惠券帮助方法集合
 * 
 * @author zozoh(zozohtnt@gmail.com)
 */
public abstract class Coupons {

    /**
     * @param coupon
     *            优惠券对象
     * @param scope
     *            限制使用范围，null 表示不限制
     * @return 该优惠券是否可用
     */
    public static boolean isAvailable(WnObj coupon, String scope) {
        // 未匹配使用范围
        if (!Strings.isBlank(scope)) {
            if (coupon.has("voucher_scope")) {
                List<String> scopes = coupon.getAsList("voucher_scope", String.class);
                if (!scopes.isEmpty() && !scopes.contains(scope)) {
                    return false;
                }
            }
        }

        // 已经使用了
        if (coupon.has("voucher_payId"))
            return false;

        // 过期
        if (coupon.getLong("voucher_endTime") < System.currentTimeMillis())
            return false;

        // 未到有效期
        if (coupon.getLong("voucher_startTime") > System.currentTimeMillis())
            return false;

        // 可用
        return true;
    }

    /**
     * @param coupon
     *            优惠券对象
     * @param price
     *            要测试的价格
     * @return 测试结果
     */
    public static NutMap eval(WnObj coupon, int price) {
        // 价格不能为负，还能倒找你钱不成？！
        if (price <= 0) {
            throw Er.create("e.cmd.voucher_test_coupon.price_negative_or_zero", price);
        }

        // 准备返回结果
        NutMap re = new NutMap();
        re.put("price", price);

        // 打折后价格
        int price_new = -1;

        // 得到 coupon 的信息
        int vo_cnd = coupon.getInt("voucher_condition");
        double vo_val = coupon.getDouble("voucher_discount");

        // 未满足最低金额
        if (vo_cnd > 0 && price < vo_cnd) {
            throw Er.create("e.cmd.voucher_test_coupon.not_reach_condition", price + "<" + vo_cnd);
        }
        // 满减,不能为负数
        else if (vo_val > 1) {
            price_new = (int) Math.max(price - vo_val, 0);
        }
        // 打折
        else {
            price_new = (int) (price * vo_val);
        }

        // 这个优惠券根本没用吧 -_-!
        if (price_new == price) {
            throw Er.create("e.cmd.voucher_test_coupon.no_need");
        }
        // 得到新价格
        re.put("ok", true);
        re.put("price_new", price_new);

        // 返回
        return re;
    }

}
