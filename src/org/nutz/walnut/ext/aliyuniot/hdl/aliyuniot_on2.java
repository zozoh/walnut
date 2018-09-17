package org.nutz.walnut.ext.aliyuniot.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.JvmHdlParamArgs;
import org.nutz.walnut.impl.box.WnSystem;

/**
 * 设置继电器状态为开
 * 
 * @author wendal
 *
 */
@JvmHdlParamArgs(value="cqn", regex="^dry$")
public class aliyuniot_on2 extends aliyuniot_pub {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        hc.params.setv("msg", Json.toJson(new NutMap("method", "setv").setv("data", new NutMap("PowerSwitch", "on").setv("OpMode", "remote"))));
        super.invoke(sys, hc);
    }
}
