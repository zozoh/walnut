package org.nutz.walnut.ext.thing.hdl;

import java.util.HashMap;
import java.util.Map;

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
        WnObj oT = Things.checkThIndex(sys, hc);

        // 这个 Thing 必须是有效的
        if (oT.getInt("th_live") == Things.TH_DEAD) {
            throw Er.create("e.cmd.thing.gone", oT.id());
        }

        // 看看是否要读取 detail/media/attachment 的映射等东东
        if (hc.params.is("full")) {
            // detail
            String detail = "";
            if (oT.len() > 0) {
                detail = sys.io.readText(oT);
            }
            oT.put("th_detail", detail);

            // 媒体映射
            oT.put("th_media_map", __gen_file_map(sys, oT, "th_media_ids"));

            // 附件映射
            oT.put("th_attachment_map", __gen_file_map(sys, oT, "th_attachment_ids"));
        }

        // 记录输出
        hc.output = oT;
    }

    private Map<String, String> __gen_file_map(WnSystem sys, WnObj oT, String key) {
        Map<String, String> map = new HashMap<String, String>();
        if (oT.has(key)) {
            String[] ids = oT.getArray(key, String.class);
            for (String id : ids) {
                WnObj o = sys.io.get(id);
                if (null != o) {
                    map.put(o.name(), o.id());
                }
            }
        }
        return map;
    }

}
