package org.nutz.walnut.ext.thing.hdl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.nutz.lang.util.NutBean;
import org.nutz.walnut.api.err.Er;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.ext.thing.Things;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("cnqihbslVNHQ")
public class thing_get implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) {
        // 得到对应对 Thing
        WnObj oTs = Things.checkThingSet(hc.oRefer);
        WnObj oT = Things.checkThIndex(sys, hc);

        // 这个 Thing 必须是有效的
        if (oT.getInt("th_live") == Things.TH_DEAD) {
            throw Er.create("e.cmd.thing.gone", oT.id());
        }

        // 补充上 ThingSet 的集合名称
        oT.put("th_set_nm", oTs.name());

        // 看看是否要读取 detail/media/attachment 的映射等东东
        if (hc.params.is("full")) {
            // detail
            String detail = "";
            if (oT.len() > 0) {
                detail = sys.io.readText(oT);
            }
            oT.put("content", detail);

            // 媒体映射
            __set_file_map(sys, oT, "media");

            // 附件映射
            __set_file_map(sys, oT, "attachment");
        }

        // 记录输出
        hc.output = oT;
    }

    private void __set_file_map(WnSystem sys, WnObj oT, String mode) {
        String key = "th_" + mode + "_ids";
        if (oT.has(key)) {
            String[] ids = oT.getArray(key, String.class);
            Map<String, String> map = new TreeMap<String, String>();
            List<NutBean> list = new ArrayList<>(ids.length);
            for (String id : ids) {
                WnObj o = sys.io.get(id);
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
