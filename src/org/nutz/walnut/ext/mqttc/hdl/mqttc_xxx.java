package org.nutz.walnut.ext.mqttc.hdl;

import org.nutz.mvc.Mvcs;
import org.nutz.walnut.ext.mqttc.MqttClientService;
import org.nutz.walnut.impl.box.JvmHdl;

public abstract class mqttc_xxx implements JvmHdl {

    protected MqttClientService _mqttc;

    public MqttClientService mqttService() {
        if (_mqttc == null) {
            _mqttc = Mvcs.getIoc().get(MqttClientService.class);
        }
        return _mqttc;
    }
}
