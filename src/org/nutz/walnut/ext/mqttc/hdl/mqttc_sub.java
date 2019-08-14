package org.nutz.walnut.ext.mqttc.hdl;

import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class mqttc_sub extends mqttc_xxx {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        String topic = hc.params.val_check(0);
        String handler = hc.params.val_check(1);
        mqttService(hc.ioc).addSub(sys.me.name(), hc.oRefer.name(), handler, topic);
    }
}