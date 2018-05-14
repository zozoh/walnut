package org.nutz.walnut.ext.thing.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.ThingAction;
import org.nutz.walnut.ext.thing.util.Things;

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
        String key = "th_" + mode + "_ids";
        if (oT.has(key)) {
            String[] ids = oT.getArray(key, String.class);
            Map<String, String> map = new TreeMap<String, String>();
            List<NutBean> list = new ArrayList<>(ids.length);
            for (String id : ids) {
                WnObj o = io.get(id);
                if (null != o) {
                    map.put(o.name(), o.id());
                    list.add(o.pick("id",
                                    "nm",
                                    "thumb",
                                    "mime",
                                    "tp",
                                    "duration",
                                    "width",
                                    "height",
                                    "video_frame_count",
                                    "video_frame_rate"));
                }
            }
            oT.put("th_" + mode + "_map", map);
            oT.put("th_" + mode + "_list", list);
        }
    }

}
