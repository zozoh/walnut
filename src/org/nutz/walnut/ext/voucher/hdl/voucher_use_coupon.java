package org.nutz.walnut.ext.voucher.hdl;

import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class voucher_use_coupon extends voucher_xxx_coupon implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String id = hc.params.check("coupon_id"); //优惠卷id
        int price = hc.params.checkInt("price"); // 必须有价格
        String belongTo = hc.params.check("belongTo"); // 优惠卷的使用者
        String payId = hc.params.check("payId");
        
        WnQuery query = new WnQuery().setv("d0", "sys").setv("d1", "voucher").setv("id", id).setv("voucher_belongTo", belongTo);
        WnObj wobj = sys.io.getOne(query);
        if (wobj == null) {
            sys.err.print("e.cmd.voucher_test_coupon.not_exist");
            return;
        };
        // 看看优惠卷是否已经使用过
        if (!Strings.isBlank(wobj.getString("voucher_payId"))) {
            sys.err.print("e.cmd.voucher_test_coupon.used");
            return;
        }
        // 是否限定了使用范围
        String scope = hc.params.get("scope");
        if (!Strings.isBlank(scope)) {
            if (wobj.has("voucher_scope")) {
                List<String> scopes = wobj.getAsList("voucher_scope", String.class);
                if (!scopes.isEmpty() && !scopes.contains(scope)) {
                    sys.err.print("e.cmd.voucher_test_coupon.scope_not_match");
                    return;
                }
            }
        }
        NutMap re = acountCoupon(sys, wobj, price);
        if (!re.getBoolean("ok")) {
            sys.out.writeJson(re);
            return;
        }
        NutMap metas = new NutMap("voucher_payId", payId);
        metas.put("voucher_payStartTime", System.currentTimeMillis());
        sys.io.appendMeta(wobj, metas);
        sys.out.writeJson(re);
    }

}
