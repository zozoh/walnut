package org.nutz.walnut.ext.www.hdl;

import java.util.Date;
import org.nutz.lang.Times;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs(value = "bishlcqn", regex = "^(pager)$")
public class domain_list implements JvmHdl {
    // TODO 还没写完，做这个命令真的有意义吗？

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {

        // // 得到分页信息
        // WnPager wp = new WnPager(hc.params);
        //
        // // 得到排序信息
        // NutMap sort = null;
        // if (hc.params.has("sort")) {
        // sort = Lang.map(hc.params.check("sort"));
        // }

        // 准备结果列表
        // List<WnObj> list;

        // 获取查询条件
        WnObj oDmnHome = sys.io.fetch(null, "/domain");
        if (null == oDmnHome) {
            // list = new ArrayList<>();
        }
        // 开始查询
        else {
            WnQuery q = Wn.Q.pid(oDmnHome);

            // 是否声明了域名限制条件
            if (hc.params.vals.length > 0) {
                q.setv("dmn_host", hc.params.val(0));
            }

            // 是否声明了组限制条件
            if (hc.params.has("grp")) {
                q.setv("dmn_grp", hc.params.get("grp"));
            }

            // 是否声明了过期时间限制条件
            // 限制条件格式必须为 [2017-09-21,] 这样的日期区间
            // 支持关键字 "today", "now"
            if (hc.params.has("expi")) {
                Date now = Times.now();
                // 替换关键字
                String expi = hc.params.get("expi");
                expi = expi.replaceAll("today", Times.format("yyyy-MM-dd", now));
                expi = expi.replaceAll("now", Times.format("yyyy-MM-dd'T'HH:mm:ss", now));
                // DateRegion dr = Region.Date(expi);

                // q.setv("dmn_expi", value)

            }
        }

    }

}
