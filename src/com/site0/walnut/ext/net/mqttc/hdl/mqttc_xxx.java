package com.site0.walnut.ext.net.mqttc.hdl;

import org.nutz.ioc.Ioc;
import com.site0.walnut.ext.net.mqttc.MqttClientService;
import com.site0.walnut.impl.box.JvmHdl;

public abstract class mqttc_xxx implements JvmHdl {

    protected MqttClientService _mqttc;

    public MqttClientService mqttService(Ioc ioc) {
        if (_mqttc == null) {
            _mqttc = ioc.get(MqttClientService.class);
        }
        return _mqttc;
    }
}
