package org.nutz.walnut.ext.jsonx.hdl;

import org.nutz.lang.Lang;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.ext.jsonx.JsonXContext;
import org.nutz.walnut.ext.jsonx.JsonXFilter;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Cmds;
import org.nutz.walnut.util.Wn;
import org.nutz.walnut.util.Ws;
import org.nutz.walnut.util.ZParams;

public class jsonx_translate extends JsonXFilter {

    @Override
    protected void process(WnSystem sys, JsonXContext fc, ZParams params) {
        if (null == fc.obj)
            return;

        // 得到转换的 Map
        String json = Cmds.getParamOrPipe(sys, params, 0);
        if (Ws.isBlank(json)) {
            return;
        }
        NutMap mapping = Lang.map(json);

        Object v2 = Wn.translate(fc.obj, mapping);
        fc.obj = v2;

    }

}
