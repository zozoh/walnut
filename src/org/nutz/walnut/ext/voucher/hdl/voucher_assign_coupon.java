package org.nutz.walnut.ext.voucher.hdl;

import org.nutz.lang.Strings;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

/**
 * 把某张优惠卷指派给指定用户
 * 
 * @author wendal
 *
 */
public class voucher_assign_coupon implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String voucher_name = hc.params.val_check(0);
        String coupon_id = hc.params.val_check(1);
        String target_name = hc.params.val_check(2);
        String myName = sys.me.name();
        String coupon_ph = Wn.appendPath("/var/voucher/", myName, voucher_name, coupon_id);
        sys.nosecurity(() -> {
            WnObj wobj = Wn.checkObj(sys, coupon_ph);
            // 这张卷已经用过了
            if (!Strings.isBlank(wobj.getString("voucher_payId"))) {
                throw Er.create("e.cmd.voucher_send_coupon.used");
            }
            // 检查用户
            WnUsr ta = sys.usrService.check(target_name);
            if (null == ta) {
                throw Er.create("e.cmd.voucher_send_coupon.noexists");
            }
            // 设置
            sys.io.appendMeta(wobj, new NutMap("voucher_uid", ta.id()));
            sys.io.appendMeta(wobj, new NutMap("voucher_unm", ta.name()));
        });
    }

}
