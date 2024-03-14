package com.site0.walnut.ext.net.mqttc.hdl;

import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;

@JvmHdlParamArgs("reload")
public class mqttc_load extends mqttc_xxx {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        mqttService(hc.ioc).get(sys.getMyName(), hc.oRefer.name(), hc.params.is("reload"));
    }

}
