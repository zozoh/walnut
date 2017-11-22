package org.nutz.walnut.ext.voucher.hdl;

import java.util.List;

import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.WnPager;

public class voucher_list_coupon implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String voucher_name = hc.params.get("name"); // 指定活动
        String belongTo = hc.params.get("belongTo"); // 指定用户
        String payId = hc.params.get("payId");
        String startTime = hc.params.get("startTime");
        String endTime = hc.params.get("endTime");
        String scope = hc.params.get("scope");
        
        WnQuery query = new WnQuery().setv("d0", "var").setv("d1", "voucher").setv("race", WnRace.FILE.toString());
        // 指定用户
        if (!Strings.isBlank(belongTo)) {
            query.setv("voucher_belongTo", belongTo);
        }
        // 指定支付单
        if (!Strings.isBlank(payId)) {
            query.setv("voucher_payId", payId);
        }
        // 指定起效时间
        if (!Strings.isBlank(startTime)){
            query.setv("voucher_startTime", new NutMap("$gte", Times.ams(startTime)));
        }
        // 指定失效时间
        if (!Strings.isBlank(endTime)){
            query.setv("voucher_endTime", new NutMap("$lte", Times.ams(endTime)));
        }
        if (!Strings.isBlank(scope)) {
            query.setv("voucher_scope", scope);
        }
        sys.nosecurity(()->{
            // 指定活动
            if (!Strings.isBlank(voucher_name)) {
                WnObj wobj = sys.io.check(null, "/var/voucher/"+sys.me.name() + "/" + voucher_name);
                query.setv("pid", wobj.id());
            }
            WnPager pager = new WnPager(hc.params);
            query.skip(pager.skip).limit(pager.limit);
            List<WnObj> list = sys.io.query(query);
            Cmds.output_objs(sys, hc.params, pager, list, false);
        });
    }

}
