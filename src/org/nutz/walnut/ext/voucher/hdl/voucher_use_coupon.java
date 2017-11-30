package org.nutz.walnut.ext.voucher.hdl;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.ext.voucher.Coupons;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class voucher_use_coupon implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String id = hc.params.val_check(0); // 优惠卷id
        int price = hc.params.val_check_int(1); // 必须有价格
        String belongTo = hc.params.val_check(2); // 优惠券所属者

        // 支付单 ID
        String payId = hc.params.check("payId");

        // 检查优惠卷的使用者
        WnUsr u = sys.usrService.check(belongTo);
        if (null == u) {
            throw Er.create("e.cmd.voucher_send_coupon.noexists");
        }

        // 查找优惠券
        WnQuery query = new WnQuery().setv("d0", "var")
                                     .setv("d1", "voucher")
                                     .setv("id", id)
                                     .setv("voucher_uid", u.id());
        WnObj coupon = sys.io.getOne(query);
        if (coupon == null) {
            throw Er.create("e.cmd.voucher_test_coupon.not_exist");
        }

        // 看看优惠卷是否已经使用过
        if (!Coupons.isAvailable(coupon, hc.params.get("scope"))) {
            throw Er.create("e.cmd.voucher_use_coupon.noava");
        }

        // 试算优惠券，看看是否满足使用条件
        NutMap re = Coupons.eval(coupon, price);

        // 不满足，打印输出
        if (!re.getBoolean("ok")) {
            sys.out.writeJson(re);
            return;
        }

        // 标识一下优惠券已被使用
        NutMap metas = new NutMap("voucher_payId", payId);
        metas.put("voucher_payTime", System.currentTimeMillis());
        sys.io.appendMeta(coupon, metas);

        // 输出
        sys.out.writeJson(coupon);
    }

}
