package org.nutz.walnut.ext.mqttc.hdl;

import org.nutz.ioc.Ioc;
import org.nutz.walnut.ext.mqttc.MqttClientService;
import org.nutz.walnut.impl.box.JvmHdl;

public abstract class mqttc_xxx implements JvmHdl {

    protected MqttClientService _mqttc;

    public MqttClientService mqttService(Ioc ioc) {
        if (_mqttc == null) {
            _mqttc = ioc.get(MqttClientService.class);
        }
        return _mqttc;
    }
}
