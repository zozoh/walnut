package com.site0.walnut.ext.net.mqttc.hdl;

import java.io.ByteArrayOutputStream;

import org.eclipse.paho.client.mqttv3.MqttClient;
import com.site0.walnut.api.io.WnObj;
import com.site0.walnut.impl.box.JvmHdlContext;
import com.site0.walnut.impl.box.JvmHdlParamArgs;
import com.site0.walnut.impl.box.WnSystem;
import com.site0.walnut.util.Cmds;
import com.site0.walnut.util.Wn;
import org.nutz.web.Webs.Err;

@JvmHdlParamArgs("retained")
public class mqttc_pub extends mqttc_xxx {

    @Override
    public void invoke(WnSystem sys, JvmHdlContext hc) throws Exception {
        MqttClient client = mqttService(hc.ioc).get(sys.getMyName(), hc.oRefer.name(), false);
        String topic = hc.params.val_check(0);
        byte[] payload = null;
        if (hc.params.has("f")) {
            ByteArrayOutputStream bao = new ByteArrayOutputStream();
            WnObj wobj = sys.io.check(null, Wn.normalizeFullPath(hc.params.get("f"), sys));
            if (wobj.len() > 8096) {
                throw Err.create("e.cmd.mqttc.pub.payload_over_limit");
            }
            sys.io.readAndClose(wobj, bao);
            payload = bao.toByteArray();
        } else {
            String data = Cmds.checkParamOrPipe(sys, hc.params, 1);
            payload = data.getBytes();
            if (payload.length > 8096) {
                throw Err.create("e.cmd.mqttc.pub.payload_over_limit");
            }
        }
        if (payload.length < 1) {
            throw Err.create("e.cmd.mqttc.pub.payload_is_emtry");
        }
        client.publish(topic, payload, hc.params.getInt("qos", 0), hc.params.is("retained"));
    }

}
