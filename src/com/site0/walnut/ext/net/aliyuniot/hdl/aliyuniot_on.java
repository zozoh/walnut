package com.site0.walnut.ext.net.aliyuniot.hdl;

import org.nutz.json.Json;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

/**
 * 设置继电器状态为开
 * 
 * @author wendal
 *
 */
@JvmHdlParamArgs(value="cqn", regex="^dry$")
public class aliyuniot_on extends aliyuniot_shadow {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        hc.params.setv("u", Json.toJson(new NutMap("PowerSwitch", "on").setv("OpMode", "remote")));
        super.invoke(sys, hc);
    }
}
