package com.site0.walnut.ext.net.mqttc.hdl;

import org.eclipse.paho.client.mqttv3.MqttClient;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.WnSystem;

public class mqttc_unpub extends mqttc_xxx {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        MqttClient client = mqttService(hc.ioc).get(sys.getMyName(), hc.oRefer.name(), false);
        String topic = hc.params.val_check(0);
        client.unsubscribe(topic);
    }

}