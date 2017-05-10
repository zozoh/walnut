package org.nutz.walnut.ext.payment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Strings;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnIo;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;

public abstract class WnPays {

    public static WnObj getPayHome(WnIo io) {
        return io.createIfNoExists(null, "/var/payment", WnRace.DIR);
    }

    public static WnPayInfo genPayInfo(String bu, String se) {
        WnPayInfo wpi = new WnPayInfo();
        fillBuyer(wpi, bu);
        fillBuyer(wpi, se);
        return wpi;
    }

    public static void fillBuyer(WnPayInfo wpi, String bu) {
        if (Strings.isBlank(bu))
            return;

        String[] ss = bu.split(":");
        wpi.buyer_tp = Strings.trim(Strings.sBlank(ss[0], null));
        wpi.buyer_id = Strings.trim(Strings.sBlank(ss[1], null));
        wpi.buyer_nm = Strings.trim(Strings.sBlank(ss[2], null));
    }

    public static void fillSeller(WnPayInfo wpi, String se) {
        if (Strings.isBlank(se))
            return;

        String[] ss = se.split(":");
        wpi.seller_id = Strings.trim(Strings.sBlank(ss[0], null));
        wpi.seller_nm = Strings.trim(Strings.sBlank(ss[1], null));
    }

    public static void fillFee(WnPayInfo wpi, String fee) {
        fee = Strings.trim(fee);
        if (Strings.isEmpty(fee))
            return;

        Matcher m = Pattern.compile("^(([0-9]*[.]?[0-9]+)|(0-9)+)([A-Z]*)")
                           .matcher(fee.toUpperCase());

        // 合法
        if (m.find()) {
            wpi.fee = Float.parseFloat(m.group(1));
            wpi.cur = Strings.sBlank(m.group(4), null);
        }
        // 非法
        else {
            throw Er.create("e.pay.invalid.fee", fee);
        }
    }

    // 阻止实例化
    private WnPays() {}
}
