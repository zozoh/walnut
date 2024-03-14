package com.site0.walnut.ext.net.mqttc;

import java.io.OutputStream;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.nutz.lang.random.R;
import org.nutz.lang.util.NutMap;
import com.site0.walnut.api.io.WnIo;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.api.io.WnRace;
import com.site0.walnut.util.Wn;
import com.site0.walnut.util.WnRun;

public class WnMqttSub implements IMqttMessageListener {

    public String user;
    public String mqttc;
    public String topic;
    public String message_path;
    public String handler_path;
    public WnRun run;
    public WnIo io;

    public WnMqttSub(String user, String mqttc, String topic, String message_path, String handler_path, WnRun run, WnIo io) {
        super();
        this.user = user;
        this.mqttc = mqttc;
        this.topic = topic;
        this.message_path = message_path;
        this.handler_path = handler_path;
        this.run = run;
        this.io = io;
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        WnObj wobj = io.createIfExists(null, message_path + R.UU32(), WnRace.FILE);
        NutMap metas = new NutMap();
        metas.put("mqtt_topic", topic);
        metas.put("mqtt_qos", message.getQos());
        metas.put("expi", Wn.now() + 10 * 60 * 1000); // 10分钟过期
        io.appendMeta(wobj, metas);
        try (OutputStream out = io.getOutputStream(wobj, 0)) {
            out.write(message.getPayload());
        }
        run.exec("mqttc", user,  "jsc " + handler_path + " id:" + wobj.id());
    }
}