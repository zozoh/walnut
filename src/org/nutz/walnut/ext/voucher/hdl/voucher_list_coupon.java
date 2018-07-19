package org.nutz.walnut.ext.voucher.hdl;

import java.util.List;
import java.util.Map;

import org.nutz.lang.Lang;
import org.nutz.lang.Strings;
import org.nutz.lang.Times;
import org.nutz.lang.util.NutMap;
import org.nutz.lang.util.Region;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.api.usr.WnUsr;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.WnPager;

@JvmHdlParamArgs("cqn")
public class voucher_list_coupon implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String voucher_name = hc.params.get("name"); // 指定活动

        WnQuery query = new WnQuery().setv("d0", "var");
        query.setv("d1", "voucher").setv("race", WnRace.FILE);
        // 指定条件
        if (hc.params.has("match")) {
            NutMap match = Lang.map(hc.params.get("match"));
            for (Map.Entry<String, Object> en : match.entrySet()) {
                String key = en.getKey();
                Object val = en.getValue();
                // 开始时间：转换
                if ("voucher_startTime".equals(key)) {
                    query.setv(key, Region.Longf("[%d,)", Times.ams(val.toString())));
                }
                // 结束时间：转换
                else if ("voucher_endTime".equals(key)) {
                    query.setv(key, Region.Longf("(,%d)", Times.ams(val.toString())));
                }
                // 用户: 转换成 ID
                else if ("user".equals(key) && null != val) {
                    sys.nosecurity(() -> {
                        WnUsr u = sys.usrService.check(val.toString());
                        query.setv("voucher_uid", u.id());
                    });
                }
                // 其他 copy
                else {
                    query.setv(key, val);
                }
            }
        }
        sys.nosecurity(() -> {
            // 指定活动
            if (!Strings.isBlank(voucher_name)) {
                WnObj wobj = sys.io.check(null,
                                          "/var/voucher/" + sys.me.name() + "/" + voucher_name);
                query.setv("pid", wobj.id());
            }
            // 否则只是搜索自己创建的代金券
            else {
                query.setv("c", sys.me.name());
            }
            WnPager pager = new WnPager(hc.params);
            pager.setupQuery(sys, query);
            List<WnObj> list = sys.io.query(query);
            
            Cmds.output_objs(sys, hc.params, pager, list, false);
        });
    }

}
