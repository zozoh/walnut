package org.nutz.walnut.ext.www.hdl;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.trans.Atom;
import org.nutz.walnut.api.auth.WnAccount;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.payment.WnPayObj;
import org.nutz.walnut.ext.payment.WnPayment;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

public class domain_apply implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        sys.nosecurity(new Atom() {
            public void run() {
                __do_apply(sys, hc);
            }
        });
    }

    protected void __do_apply(WnSystem sys, JvmHdlContext hc) {
        // 得到支付接口
        WnPayment pay = hc.ioc.get(WnPayment.class);

        // 得到支付单对象
        String poId = hc.params.val_check(0);
        WnPayObj po = pay.get(poId, false);

        // 支付单必须没有被应用过
        if (po.isApplied())
            return;

        // 得到关键信息
        String host = po.getString("buyer_host");
        String goods = po.getString("buy_for").toUpperCase();

        // 分析一下参数购买类型
        Matcher m = Pattern.compile("^(M([1-9])|Y([1-9]))$").matcher(goods);
        String M, Y;
        if (m.find()) {
            M = m.group(2);
            Y = m.group(3);
        }
        // 否则
        else {
            throw Er.create("e.cmd.domain.apply.invalidGoods", goods);
        }

        // 得到购买者信息
        String buyer = po.getBuyerName();
        WnAccount bu = sys.auth.checkAccount(buyer);

        // 准备查询条件
        WnObj oDmnHome = sys.io.check(null, "/domain");

        // 找到对应记录
        // List<WnObj> oDmns = new ArrayList<>(2);
        WnObj oD = __get_host_obj(sys, bu, oDmnHome, host);
        // oDmns.add(oD);

        // 计算最终的过期时间
        long expiInMs = __count_dmn_expi(M, Y, oD);

        // // 还需要顺便修改 www.xxx.xx 的域名映射
        // if (WWW.isMainHost(host)) {
        // oD = __join_host(sys, bu, oDmns, oDmnHome, "www." + host);
        // oDmns.add(oD);
        // }

        // 应用修改
        oD.setv("dmn_expi", expiInMs);
        sys.io.set(oD, "^(dmn_expi)$");
    }

    protected long __count_dmn_expi(String M, String Y, WnObj oD) {
        Calendar expi = Calendar.getInstance();

        // 考虑到追加购买的情况(记录里的和当前的，哪个大用哪个)
        long expiInMs = expi.getTimeInMillis();
        long dmn_expi = oD.getLong("dmn_expi", 0);
        if (expiInMs < dmn_expi) {
            expi.setTimeInMillis(dmn_expi);
        }

        // 按月
        if (!Strings.isBlank(M)) {
            int months = Integer.parseInt(M);
            expi.add(Calendar.MONTH, months);
        }
        // 按年
        else if (!Strings.isBlank(Y)) {
            int years = Integer.parseInt(Y);
            expi.add(Calendar.YEAR, years);
        }
        // 不可能
        else {
            throw Lang.impossible();
        }

        // 最后得到要设置的最终日期毫秒数
        expiInMs = expi.getTimeInMillis();
        return expiInMs;
    }

    private WnObj __get_host_obj(WnSystem sys, WnAccount bu, WnObj oDmnHome, String host) {
        WnQuery q = Wn.Q.pid(oDmnHome);
        WnObj oD = sys.io.getOne(q.setv("dmn_host", host));
        if (null == oD) {
            oD = sys.io.create(oDmnHome, host, WnRace.FILE);
            oD.setv("dmn_grp", bu.getGroupName());
            oD.setv("dmn_host", host);
            sys.io.set(oD, "^dmn_.+$");
        }
        return oD;
    }

}
