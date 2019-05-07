package org.nutz.walnut.ext.titanium.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.impl.box.JvmHdl;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;
import org.nutz.walnut.util.Wn;

@JvmHdlParamArgs("cqn")
public class ti_config implements JvmHdl {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String name = hc.params.get("name", "config.json");
        String aph = "~/.ti/" + name;
        WnObj oConfig = Wn.getObj(sys, aph);
        if (null == oConfig) {
            sys.out.println("{}");
        }
        // 解析
        else {
            NutMap map = sys.io.readJson(oConfig, NutMap.class);
            String json = Json.toJson(map, hc.jfmt);
            sys.out.print(json);
        }
    }

}
