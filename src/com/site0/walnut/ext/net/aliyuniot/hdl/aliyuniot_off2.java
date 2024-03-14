package com.site0.walnut.ext.net.aliyuniot.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

/**
 * 设置继电器状态为关
 * 
 * @author wendal
 *
 */
@JvmHdlParamArgs(value="cqn", regex="^dry$")
public class aliyuniot_off2 extends aliyuniot_pub {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        hc.params.setv("msg", Json.toJson(new NutMap("method", "setv").setv("data", new NutMap("PowerSwitch", "off").setv("OpMode", "remote"))));
        super.invoke(sys, hc);
    }
}
