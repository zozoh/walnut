package org.nutz.walnut.ext.mqttc.hdl;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class mqttc_unpub extends mqttc_xxx {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        MqttClient client = mqttService(hc.ioc).get(sys.me.name(), hc.oRefer.name(), false);
        String topic = hc.params.val_check(0);
        client.unsubscribe(topic);
    }

}