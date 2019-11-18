package org.nutz.walnut.ext.payment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.WnSystem;

public abstract class WnPays {

    // ..............................................................
    public static final String KEY_BRIEF = "brief";
    public static final String KEY_SEND_AT = "send_at";
    public static final String KEY_CLOSE_AT = "close_at";
    public static final String KEY_APPLY_AT = "apply_at";
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
    public static final String KEY_ST = "st"; // @see WnPay3xStatus
    public static final String KEY_RETURN_URL = "pay_return_url";
    public static final String KEY_RE_TP = "re_tp";
    public static final String KEY_RE_OBJ = "re_obj";
    public static final String KEY_CLIENT_IP = "client_ip";
    // ..............................................................

    public static WnObj getPayHome(WnIo io) {
        return io.createIfNoExists(null, "/var/payment", WnRace.DIR);
    }

    public static WnPayInfo genPayInfo(String bu, String se) {
        WnPayInfo wpi = new WnPayInfo();
        fillBuyer(wpi, bu);
        fillSeller(wpi, se);
        return wpi;
    }

    public static void fillBuyer(WnPayInfo wpi, String bu) {
        if (Strings.isBlank(bu))
            return;

        // 买家类型
        int pos = bu.indexOf(':');
        if (pos > 0) {
            wpi.buyer_tp = bu.substring(0, pos);
            wpi.buyer_id = Strings.trim(bu.substring(pos + 1));
        } else {
            // 填充 ID
            if (bu.startsWith("id:")) {
                wpi.buyer_id = bu.substring(3);
            }
            // 填充名称或其他可登陆信息
            else {
                wpi.buyer_id = bu;
            }
        }
    }

    public static void fillSeller(WnPayInfo wpi, String se) {
        if (Strings.isBlank(se))
            return;

        // 填充 ID
        if (se.startsWith("id:")) {
            wpi.seller_id = se.substring(3);
        }
        // 填充名称或其他可登陆信息
        else {
            wpi.seller_id = se;
        }
    }

    public static void fillFee(WnPayInfo wpi, String fee) {
        fee = Strings.trim(fee);
        if (Strings.isEmpty(fee))
            return;

        Matcher m = Pattern.compile("^(([0-9]*[.]?[0-9]+)|(0-9)+)([A-Z]*)")
                           .matcher(fee.toUpperCase());

        // 合法
        if (m.find()) {
            wpi.fee = Integer.parseInt(m.group(1));
            wpi.cur = Strings.sBlank(m.group(4), null);
            wpi.price = wpi.fee;
        }
        // 非法
        else {
            throw Er.create("e.pay.invalid.fee", fee);
        }
    }

    // 阻止实例化
    private WnPays() {}

    public static void try_callback(WnSystem sys, WnPayObj po) {
        // 如果支付单成功，且没执行过回调的话，执行回调
        if (po.isStatusOk() && po.getLong(WnPays.KEY_APPLY_AT, 0) <= 0) {
            String cmdText = sys.io.readText(po);
            // 有效的回调
            if (!Strings.isBlank(cmdText)) {
                sys.exec(cmdText);
            }
            // 标识一下支付单已经被应用过了
            po.setv(WnPays.KEY_APPLY_AT, System.currentTimeMillis());
            sys.io.set(po, "^(" + WnPays.KEY_APPLY_AT + ")$");
            // 试图通过 websocket 通知一下
            sys.execf("websocket event id:%s done", po.id());
        }
    }
}
