package org.nutz.walnut.ext.thing.impl;

import java.util.Map;
import java.util.TreeMap;

import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnQuery;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.ext.thing.ThingAction;
import org.nutz.walnut.ext.thing.util.Things;
import org.nutz.walnut.util.Wn;

public class GetThingAction extends ThingAction<WnObj> {

    private String id;

    private boolean isFull;

    private boolean quiet;

    public GetThingAction setId(String id) {
        this.id = id;
        return this;
    }

    public GetThingAction setFull(boolean isFull) {
        this.isFull = isFull;
        return this;
    }

    public GetThingAction setQuiet(boolean quiet) {
        this.quiet = quiet;
        return this;
    }

    public WnObj invoke() {
        // 得到对应对 Thing
        WnObj oT = this.getThIndex(id);

        // 这个 Thing 必须是有效的
        if (null == oT || oT.getInt("th_live") == Things.TH_DEAD) {
            if (quiet)
                return null;
            throw Er.create("e.thing.get.noexists", oT.id());
        }

        // 补充上 ThingSet 的集合名称
        oT.put("th_set_nm", oTs.name());

        // 看看是否要读取 detail/media/attachment 的映射等东东
        if (isFull) {
            // detail
            String detail = "";
            if (oT.len() > 0) {
                detail = io.readText(oT);
            }
            oT.put("content", detail);

            // 媒体映射
            __set_file_map(oT, "media");

            // 附件映射
            __set_file_map(oT, "attachment");
        }

        // 返回
        return oT;
    }

    private void __set_file_map(WnObj oT, String mode) {
        String key = "th_" + mode + "_list";
        // 没有的话，如果还是有媒体的，就搞一下同步
        if (!oT.has(key) && oT.getInt("th_" + mode + "_nb") > 0) {
            WnObj oData = Things.dirTsData(io, this.oTs);
            WnObj oDir = io.fetch(oData, oT.id() + "/" + mode);
            if (null != oDir) {
                WnQuery q = Wn.Q.pid(oDir);
                q.setv("race", WnRace.FILE);
                Things.update_file_count(io, oT, mode, q);
            }
            // 没有媒体目录的的话，就表搞了
            else {
                return;
            }
        }
        // 有的话就用
        if (oT.has(key)) {
            NutMap[] fs = oT.getArray(key, NutMap.class);
            Map<String, String> map = new TreeMap<String, String>();
            for (NutMap f : fs) {
                if (null != f) {
                    map.put(f.getString("nm"), f.getString("id"));
                }
            }
            oT.put("th_" + mode + "_map", map);
        }
    }

}
