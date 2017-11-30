package org.nutz.walnut.ext.voucher.hdl;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.ext.voucher.Coupons;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class voucher_test_coupon implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String id = hc.params.val_check(0); // 优惠卷id
        int price = hc.params.val_check_int(1); // 价格

        WnQuery query = new WnQuery().setv("d0", "var").setv("d1", "voucher").setv("id", id);
        WnObj wobj = sys.io.getOne(query);
        if (wobj == null) {
            throw Er.create("e.cmd.voucher_test_coupon.not_exist");
        }

        // 如果指定了范围，则需要检测优惠券的有效性
        String scope = hc.params.get("scope");
        if (!Strings.isBlank(scope))
            if (!Coupons.isAvailable(wobj, scope)) {
                throw Er.create("e.cmd.voucher_test_coupon.noava");
            }

        // 输出评估结果
        sys.out.writeJson(Coupons.eval(wobj, price));
    }

}
