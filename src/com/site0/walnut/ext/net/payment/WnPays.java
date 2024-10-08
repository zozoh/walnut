package com.site0.walnut.ext.net.payment;

import org.nutz.lang.Strings;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Wn;

public abstract class WnPays {

    // ..............................................................
    public static final String KEY_BRIEF = "brief";
    public static final String KEY_SEND_AT = "send_at";
    public static final String KEY_CLOSE_AT = "close_at";
    public static final String KEY_APPLY_AT = "apply_at";
    public static final String KEY_APPLY_RE = "apply_re";
    public static final String KEY_SELLER_NM = "seller_nm";
    public static final String KEY_SELLER_ID = "seller_id";
    public static final String KEY_BUYER_NM = "buyer_nm";
    public static final String KEY_BUYER_ID = "buyer_id";
    public static final String KEY_BUYER_TP = "buyer_tp";
    public static final String KEY_PAY_TP = "pay_tp";
    public static final String KEY_PAY_TARGET = "pay_target";
    public static final String KEY_CUR = "cur";
    public static final String KEY_FEE = "fee";
    public static final String KEY_PRICE = "price";
    public static final String KEY_ST = "pay_st"; // @see WnPay3xStatus
    public static final String KEY_RETURN_URL = "pay_return_url";
    public static final String KEY_RE_TP = "re_tp";
    public static final String KEY_RE_OBJ = "re_obj";
    public static final String KEY_CLIENT_IP = "client_ip";
    // ..............................................................

    public static WnObj getPayHome(WnIo io) {
        return io.createIfNoExists(null, "/var/payment", WnRace.DIR);
    }

    // 阻止实例化
    private WnPays() {}

    public static void try_callback(WnSystem sys, WnPayObj po) {
        // 如果支付单成功，且没执行过回调的话，执行回调
        if (po.isStatusOk() && po.getLong(WnPays.KEY_APPLY_AT, 0) <= 0) {
            String cmdText = sys.io.readText(po);
            String re = null;
            // 有效的回调
            if (!Strings.isBlank(cmdText)) {
                re = sys.exec2(cmdText);
            }
            // 标识一下支付单已经被应用过了
            po.setv(WnPays.KEY_APPLY_AT, Wn.now());
            po.setv(WnPays.KEY_APPLY_RE, re);
            sys.io.set(po, "^(" + WnPays.KEY_APPLY_AT + "|" + WnPays.KEY_APPLY_RE + ")$");
            // 试图通过 websocket 通知一下
            sys.execf("websocket event id:%s done", po.id());
        }
    }
}
