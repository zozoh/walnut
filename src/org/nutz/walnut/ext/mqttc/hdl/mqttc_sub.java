package org.nutz.walnut.ext.mqttc.hdl;

import java.io.OutputStream;

import org.apache.sshd.common.util.io.NullInputStream;
import org.apache.sshd.common.util.io.NullOutputStream;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import org.nutz.walnut.api.io.WnObj;
import org.nutz.walnut.api.io.WnRace;
import org.nutz.walnut.impl.box.JvmHdlContext;
import org.nutz.walnut.impl.box.WnSystem;

public class mqttc_sub extends mqttc_xxx {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        MqttClient client = mqttService(hc.ioc).get(sys.me.name(), hc.oRefer.name(), false);
        String topic = hc.params.val_check(0);
        String handler = hc.params.val_check(1);
        String message_path = "/home/" + sys.me.name() + "/.mqttc/" + hc.oRefer.name() + "/message/";
        String handler_path = "/home/" + sys.me.name() + "/.mqttc/" + hc.oRefer.name() + "/handler/" + handler;
        client.subscribe(topic, new IMqttMessageListener() {
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                WnObj wobj = sys.io.createIfExists(null, message_path + R.UU32(), WnRace.FILE);
                NutMap metas = new NutMap();
                metas.put("mqtt_topic", topic);
                metas.put("mqtt_qos", message.getQos());
                metas.put("expi", System.currentTimeMillis() + 10 * 60 * 1000); // 10分钟过期
                sys.io.appendMeta(wobj, metas);
                try (OutputStream out = sys.io.getOutputStream(wobj, 0)) {
                    out.write(message.getPayload());
                }
                NullOutputStream nop = new NullOutputStream();
                sys.exec("jsc " + handler_path + " id:" + wobj.id(), nop, nop, new NullInputStream());
            }
        });
    }
}