package org.nutz.walnut.ext.voucher.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class voucher_test_coupon extends voucher_xxx_coupon implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String id = hc.params.check("coupon_id"); //优惠卷id
        
        WnQuery query = new WnQuery().setv("d0", "var").setv("d1", "voucher").setv("id", id);
        WnObj wobj = sys.io.getOne(query);
        if (wobj == null) {
            sys.err.print("e.cmd.voucher_test_coupon.not_exist");
            return;
        };
        sys.out.writeJson(acountCoupon(sys, wobj, hc.params.checkInt("price")));
    }

}
