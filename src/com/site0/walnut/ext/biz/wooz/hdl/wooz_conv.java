package com.site0.walnut.ext.biz.wooz.hdl;

import org.nutz.json.Json;
import com.site0.walnut.ext.biz.wooz.WoozMap;
import com.site0.walnut.ext.biz.wooz.WoozPoint;
import com.site0.walnut.ext.biz.wooz.WoozRoute;
import com.site0.walnut.ext.biz.wooz.WoozTools;
import com.site0.walnut.impl.box.JvmHdl;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;

@JvmHdlParamArgs(value="cqn")
public class wooz_conv implements JvmHdl {

    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String text = null;
        if (sys.pipeId > 0) {
            text = sys.in.readAll();
        }
        else {
            text = sys.io.readText(sys.io.check(null, Wn.normalizeFullPath(hc.params.val_check(0), sys)));
        }
        WoozMap source = Json.fromJson(WoozMap.class, text);
        String conv_from = hc.params.get("conv_from");
        String conv_to = hc.params.get("conv_to");
        
        if (source.points != null) {
            for (WoozPoint point : source.points) {
                WoozTools.convert(point, conv_from, conv_to);
            }
        }
        if (source.route != null) {
            for (WoozRoute route : source.route) {
                WoozTools.convert(route, conv_from, conv_to);
            }
        }
        sys.out.writeJson(source, Cmds.gen_json_format(hc.params));
    }

}
