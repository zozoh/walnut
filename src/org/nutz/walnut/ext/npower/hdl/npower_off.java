package org.nutz.walnut.ext.npower.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 设置继电器状态为关
 * 
 * @author wendal
 *
 */
@JvmHdlParamArgs("cqn")
public class npower_off extends npower_shadow {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        hc.params.setv("u", Json.toJson(new NutMap("PowerSwitch", "off").setv("OpMode", "remote")));
        super.invoke(sys, hc);
    }
}
