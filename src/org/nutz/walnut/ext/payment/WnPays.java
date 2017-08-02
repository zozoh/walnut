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
        if (bu.startsWith("%")) {
            wpi.asDusr();
            bu = Strings.trim(bu.substring(1));
        } else {
            wpi.asWnUsr();
        }

        // 填充 ID
        if (bu.startsWith("id:")) {
            wpi.buyer_id = bu.substring(3);
        }
        // 填充名称或其他可登陆信息
        else {
            wpi.buyer_nm = bu;
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
            wpi.seller_nm = se;
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
        if (po.isStatusOk() && po.getLong(WnPayObj.KEY_APPLY_AT, 0) <= 0) {
            String cmdText = sys.io.readText(po);
            // 有效的回调
            if (!Strings.isBlank(cmdText)) {
                sys.exec(cmdText);
            }
            // 标识一下支付单已经被应用过了
            po.setv(WnPayObj.KEY_APPLY_AT, System.currentTimeMillis());
            sys.io.set(po, "^(" + WnPayObj.KEY_APPLY_AT + ")$");
        }
    }
}
