package org.nutz.walnut.ext.data.o.hdl;

import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.data.o.OContext;
import org.nutz.walnut.ext.data.o.OFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.ZParams;

public class o_fetch extends OFilter {

    @Override
    protected ZParams parseParams(String[] args) {
        return ZParams.parse(args, "^(ignore|fallback)$");
    }

    @Override
    protected void process(WnSystem sys, OContext fc, ZParams params) {
        // 分析参数
        boolean ignore = params.is("ignore");
        boolean fallback = params.is("fallback");
        WnRace race = null;
        if (params.has("race")) {
            race = params.getAs("race", WnRace.class);
        }

        // 循环处理
        for (String ph : params.vals) {
            String aph = Wn.normalizeFullPath(ph, sys);
            // fallback 模式
            if (fallback) {
                WnObj o = sys.io.fetch(null, aph);
                if (null == o) {
                    continue;
                }
                fc.add(o);
                break;
            }
            // 没有就创建
            else if (null != race) {
                WnObj o = sys.io.createIfNoExists(null, aph, race);
                fc.add(o);
            }
            // 没有就忽略
            else if (ignore) {
                WnObj o = sys.io.fetch(null, aph);
                if (null != o) {
                    fc.add(o);
                }
            }
            // 必须存在
            else {
                WnObj o = sys.io.check(null, aph);
                fc.add(o);
            }
        }
    }

}
