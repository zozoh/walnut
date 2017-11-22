package org.nutz.walnut.ext.voucher.hdl;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 把某张优惠卷发送给指定用户
 * @author wendal
 *
 */
public class voucher_send_coupon implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String voucher_name = hc.params.check("name");
        String target_name = hc.params.check("user");
        String coupon_id = hc.params.check("coupon_id");
        String myName = sys.me.name();
        sys.nosecurity(()->{
            WnObj tmp = sys.io.check(null, "/var/voucher/"+ myName + "/" + voucher_name);
            WnObj wobj = sys.io.checkById(coupon_id);
            if (!tmp.id().equals(wobj.parentId())) {
                sys.err.print("e.cmd.voucher_send_coupon.not_match");
            }
            // 这张卷已经用过了
            if (!Strings.isBlank(wobj.getString("voucher_payId"))) {
                sys.err.print("e.cmd.voucher_send_coupon.used");
                return;
            }
            sys.io.appendMeta(wobj, new NutMap("voucher_belongTo", target_name));
        });
    }

}
