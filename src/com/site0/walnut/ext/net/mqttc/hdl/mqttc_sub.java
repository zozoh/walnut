package com.site0.walnut.ext.net.mqttc.hdl;

import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class mqttc_sub extends mqttc_xxx {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String topic = hc.params.val_check(0);
        String handler = hc.params.val_check(1);
        mqttService(hc.ioc).addSub(sys.getMyName(), hc.oRefer.name(), handler, topic);
    }
}